package org.exoplatform.analytics.api.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticStatus;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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

  private AnalyticsService    analyticsService;

  private String              startupDataInjectionFilePath;

  private boolean             enabled                             = false;

  public AnalyticsDataInjector(AnalyticsService analyticsService, SettingService settingService, InitParams params) {
    this.settingService = settingService;
    this.analyticsService = analyticsService;
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
    List<String> statisticStringList;
    try {
      statisticStringList = FileUtils.readLines(dataInjectionFile);
    } catch (IOException e) {
      LOG.warn("Error reading lines from file with path '{}'. Skip injection.", dataInjectionPath);
      return;
    }
    injectDataFromObjectStringList(statisticStringList);
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
      analyticsService.create(statisticData);
    }
  }

  @Override
  public void stop() {
    // Nothing to stop for now
  }

  public static void main(String[] args) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("activityId", "1");

    int numberOfDays = 120;
    Random random = new Random(1);

    String[] modules = { "social", "knowledge", "portal", "ecms", "task", "chat", "calendar" };
    String[] subModules = { "Activity stream", "Task administration", "User Profile", "Space management", "Space task project",
        "mini chat", "space calendar" };
    String[] operations = { "addActivity", "removeActivity", "joinSpace", "leftSpace", "addTask", "addChatRoom", "addEvent",
        "removeEvent" };

    for (int i = 1; i < numberOfDays; i++) {
      LocalDateTime date = LocalDateTime.now().minusDays(i);

      int numberOfItemsPerDay = random.nextInt(30);
      for (int j = 0; j < numberOfItemsPerDay; j++) {
        long userId = random.nextInt(100);
        long spaceId = random.nextInt(100);
        int moduleIndex = random.nextInt(modules.length - 1);
        int subModuleIndex = random.nextInt(subModules.length - 1);
        int operationIndex = random.nextInt(operations.length - 1);
        String module = modules[moduleIndex];
        String subModule = subModules[subModuleIndex];
        String operation = operations[operationIndex];

        StatisticData statisticData = new StatisticData(AnalyticsUtils.timeToSeconds(date.minusMinutes(j * 10)),
                                                        null,
                                                        userId,
                                                        spaceId,
                                                        module,
                                                        subModule,
                                                        operation,
                                                        StatisticStatus.OK,
                                                        null,
                                                        0L,
                                                        0L,
                                                        parameters);
        System.out.println(AnalyticsUtils.toJsonString(statisticData));
      }
    }
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

}
