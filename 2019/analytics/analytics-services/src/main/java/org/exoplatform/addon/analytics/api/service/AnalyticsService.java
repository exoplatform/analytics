package org.exoplatform.addon.analytics.api.service;

import java.util.List;

import org.exoplatform.addon.analytics.model.*;
import org.exoplatform.addon.analytics.model.search.AnalyticsSearchFilter;

public abstract class AnalyticsService {

  private AnalyticsQueueService queueService;

  public AnalyticsService(AnalyticsQueueService queueService) {
    this.queueService = queueService;
  }

  void create(StatisticData data) {
    if (data == null) {
      throw new IllegalArgumentException("Statistic data to store is mandatory");
    }

    queueService.put(data);
  }

  public abstract ChartData getChartData(AnalyticsFilter filter);

  public abstract List<StatisticData> getData(AnalyticsSearchFilter filter);

  public abstract int count(AnalyticsSearchFilter filter);

}
