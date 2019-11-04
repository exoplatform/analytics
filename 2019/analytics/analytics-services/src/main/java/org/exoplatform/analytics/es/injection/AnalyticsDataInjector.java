package org.exoplatform.analytics.es.injection;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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
import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;

/**
 * TODO: use org.exoplatform.services.bench.DataInjectorService // NOSONAR
 */
public class AnalyticsDataInjector implements Startable {
  private static final Log    LOG                                 = ExoLogger.getLogger(AnalyticsDataInjector.class);

  private static final String ANALYTICS_INJECTION_DATA_PATH_PARAM = "file.path";

  public static final int     ANALYTICS_DATA_VERSION              = 1;

  public static final String  ANALYTICS_SCOPE_NAME                = "ADDONS_ANALYTICS";

  public static final String  ANALYTICS_CONTEXT_NAME              = "ADDONS_ANALYTICS";

  public static final Context ANALYTICS_CONTEXT                   = Context.GLOBAL.id(ANALYTICS_CONTEXT_NAME);

  public static final Scope   ANALYTICS_SCOPE                     = Scope.APPLICATION.id(ANALYTICS_SCOPE_NAME);

  public static final String  DATA_INJECTION_VERSION              = "ANALYTICS_DATA_INJECTION_VERSION";

  private SettingService      settingService;

  private String              startupDataInjectionFilePath;

  private boolean             enabled                             = false;

  public AnalyticsDataInjector(SettingService settingService, InitParams params) {
    this.settingService = settingService;
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
    reinjectData();
    setDataInjected();
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void reinjectData() {
    if (StringUtils.isBlank(startupDataInjectionFilePath)) {
      LOG.info("No configured analytics data to inject");
      return;
    }
    injectDataFromFile(startupDataInjectionFilePath);
  }

  public void injectDataFromFile(String dataInjectionPath) {
    File dataInjectionFile = new File(dataInjectionPath);
    if (!dataInjectionFile.exists()) {
      LOG.warn("Configured analytics data file path '{}' doesn't exist. Skip injection.", dataInjectionPath);
      return;
    }
    try (FileInputStream input = new FileInputStream(dataInjectionFile);
        InputStreamReader reader = new InputStreamReader(input, Charsets.UTF_8);
        BufferedReader bufferedReader = IOUtils.toBufferedReader(reader);) {
      String line = null;
      do {
        List<String> statisticStringList = new ArrayList<>();
        line = bufferedReader.readLine();
        while (line != null && statisticStringList.size() < 1000) {
          statisticStringList.add(line);
          line = bufferedReader.readLine();
        }
        if (!statisticStringList.isEmpty()) {
          injectDataFromObjectStringList(statisticStringList);
        }
      } while (line != null);
    } catch (IOException e) {
      LOG.warn("Error reading lines from file with path '{}'. Skip injection.", dataInjectionPath);
    }
    StatisticDataQueueService statisticDataQueueService = CommonsUtils.getService(StatisticDataQueueService.class);
    while (statisticDataQueueService.queueSize() > 0) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
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

  public static void main(String[] args) {
    // Added to force binding DB datasource in JNDI before starting
    // PortalContainer
    RootContainer rootContainer = RootContainer.getInstance();
    rootContainer.getComponentInstanceOfType(InitialContextInitializer.class);

    PortalContainer container = PortalContainer.getInstance();
    ExoContainerContext.setCurrentContainer(container);

    try {
      AnalyticsDataInjector analyticsDataInjector = CommonsUtils.getService(AnalyticsDataInjector.class);

      File parentFolder = new File(AnalyticsDataGenerator.ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH);
      File[] listFiles = parentFolder.listFiles();
      for (File file : listFiles) {
        String filePath = file.getAbsolutePath();
        LOG.info("+++ Start Injecting file {}", filePath);

        RequestLifeCycle.begin(container);
        try {
          // reinjectData
          analyticsDataInjector.injectDataFromFile(filePath);
          // Process ES indexing queue
          IndexingOperationProcessor indexingOperationProcessor = CommonsUtils.getService(IndexingOperationProcessor.class);
          indexingOperationProcessor.process();
        } finally {
          RequestLifeCycle.end();
        }

        LOG.info("--- End injecting file {}", filePath);
      }
    } finally {
      rootContainer.stop();
    }
  }
}
