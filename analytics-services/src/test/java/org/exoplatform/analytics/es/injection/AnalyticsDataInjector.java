package org.exoplatform.analytics.es.injection;

import static org.exoplatform.analytics.utils.AnalyticsUtils.MAX_BULK_DOCUMENTS;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.analytics.api.service.StatisticDataQueueService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsDataInjector implements Startable {
  private static final Log          LOG                                 = ExoLogger.getLogger(AnalyticsDataInjector.class);

  private static final String       ANALYTICS_INJECTION_DATA_PATH_PARAM = "file.path";

  public static final int           ANALYTICS_DATA_VERSION              = 1;

  public static final String        ANALYTICS_SCOPE_NAME                = "ADDONS_ANALYTICS";

  public static final String        ANALYTICS_CONTEXT_NAME              = "ADDONS_ANALYTICS";

  public static final Context       ANALYTICS_CONTEXT                   = Context.GLOBAL.id(ANALYTICS_CONTEXT_NAME);

  public static final Scope         ANALYTICS_SCOPE                     = Scope.APPLICATION.id(ANALYTICS_SCOPE_NAME);

  public static final String        DATA_INJECTION_VERSION              = "ANALYTICS_DATA_INJECTION_VERSION";

  private PortalContainer           container;

  private ConfigurationManager      configurationManager;

  private SettingService            settingService;

  private StatisticDataQueueService statisticDataQueueService;

  private String                    startupDataInjectionFilePath;

  private boolean                   injectionStarted                    = false;

  private boolean                   enabled                             = false;

  public AnalyticsDataInjector(PortalContainer container,
                               ConfigurationManager configurationManager,
                               SettingService settingService,
                               StatisticDataQueueService statisticDataQueueService,
                               InitParams params) {
    this.container = container;
    this.configurationManager = configurationManager;
    this.settingService = settingService;
    this.statisticDataQueueService = statisticDataQueueService;
    if (params != null) {
      if (params.containsKey("enabled")) {
        this.enabled = Boolean.parseBoolean(params.getValueParam("enabled").getValue());
      }
      if (params.containsKey(ANALYTICS_INJECTION_DATA_PATH_PARAM)) {
        this.startupDataInjectionFilePath = params.getValueParam(ANALYTICS_INJECTION_DATA_PATH_PARAM).getValue();
      }
    }
  }

  @Override
  public void start() {
    if (!enabled) {
      LOG.info("Analytics data injection is disabled, skip it.");
      return;
    }

    if (isDataInjected()) {
      LOG.info("Analytics data is already injected, skip");
      return;
    }

    if (StringUtils.isBlank(startupDataInjectionFilePath)) {
      LOG.info("No configured analytics data to inject");
      return;
    }

    ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
    injectionStarted = true;
    threadExecutor.execute(() -> {
      ExoContainerContext.setCurrentContainer(this.container);
      RequestLifeCycle.begin(this.container);
      try {
        injectDataFromFile(startupDataInjectionFilePath);
        setDataInjected();
      } finally {
        injectionStarted = false;
        RequestLifeCycle.end();
        threadExecutor.shutdown();
      }
    });
  }

  public void setStartupDataInjectionFilePath(String startupDataInjectionFilePath) {
    this.startupDataInjectionFilePath = startupDataInjectionFilePath;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void injectDataFromFile(String dataInjectionPath) {
    if (StringUtils.isBlank(dataInjectionPath)) {
      throw new IllegalArgumentException("filePath is mandatory");
    }
    long startTimeMs = System.currentTimeMillis();

    File dataInjectionFile = null;
    URL url = null;
    try {
      url = configurationManager.getURL(dataInjectionPath);
    } catch (Exception e) {
      LOG.debug("Can't find file with path : {}", dataInjectionPath, e);
    }
    if (url == null) {
      dataInjectionFile = new File(dataInjectionPath);
    } else {
      dataInjectionFile = new File(url.getPath());
    }

    if (!dataInjectionFile.exists()) {
      LOG.warn("Configured analytics data file path '{}' doesn't exist. Skip injection.", dataInjectionPath);
      return;
    }

    try (FileInputStream input = new FileInputStream(dataInjectionFile);
        InputStreamReader reader = new InputStreamReader(input, Charsets.UTF_8);
        BufferedReader bufferedReader = IOUtils.toBufferedReader(reader);) {
      List<String> statisticStringList = new ArrayList<>();
      String line = bufferedReader.readLine();
      while (line != null) {
        statisticStringList.add(line);
        int statsSize = statisticStringList.size();
        if (statsSize >= MAX_BULK_DOCUMENTS) {
          LOG.info("Injecting {} stats data", statsSize);
          injectDataFromObjectStringList(statisticStringList);
          statisticStringList = new ArrayList<>();
        }

        line = bufferedReader.readLine();
      }
      if (!statisticStringList.isEmpty()) {
        injectDataFromObjectStringList(statisticStringList);
      }
    } catch (IOException e) {
      LOG.warn("Error reading lines from file with path '{}'. Skip injection.", dataInjectionPath);
    }
    while (statisticDataQueueService.queueSize() > 0) {
      try {
        LOG.info("Waiting injection finish, queue size = {}", statisticDataQueueService.queueSize());
        Thread.sleep(500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    long endTimeMs = System.currentTimeMillis();
    LOG.info("Injection time: " + (endTimeMs - startTimeMs));
  }

  public void injectDataFromObjectStringList(List<String> statisticStringList) {
    List<StatisticData> statisticList = statisticStringList.stream()
                                                           .map(statisticString -> AnalyticsUtils.fromJsonString(statisticString,
                                                                                                                 StatisticData.class))
                                                           .collect(Collectors.toList());
    injectDataFromObjectList(statisticList);
  }

  public void injectDataFromObjectList(List<StatisticData> statisticList) {
    if (statisticList.isEmpty()) {
      LOG.warn("No analytics data to inject. Skip injection.");
      return;
    }
    for (StatisticData statisticData : statisticList) {
      AnalyticsUtils.addStatisticData(statisticData);
    }
  }

  @Override
  public void stop() {
    // Nothing to stop for now
  }

  public boolean isDataInjected() {
    SettingValue<?> analyticsDataVersionValue = settingService.get(ANALYTICS_CONTEXT, ANALYTICS_SCOPE, DATA_INJECTION_VERSION);
    if (analyticsDataVersionValue == null || analyticsDataVersionValue.getValue() == null) {
      return false;
    }
    int analyticsDataVersion = Integer.parseInt(analyticsDataVersionValue.getValue().toString());
    return analyticsDataVersion >= ANALYTICS_DATA_VERSION;
  }

  private void setDataInjected() {
    settingService.set(ANALYTICS_CONTEXT,
                       ANALYTICS_SCOPE,
                       DATA_INJECTION_VERSION,
                       SettingValue.create(String.valueOf(ANALYTICS_DATA_VERSION)));
  }

  public void waitInjection() {
    while (injectionStarted) {
      try {
        LOG.info("Waiting injection finish, queue size = {}", statisticDataQueueService.queueSize());
        Thread.sleep(500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
