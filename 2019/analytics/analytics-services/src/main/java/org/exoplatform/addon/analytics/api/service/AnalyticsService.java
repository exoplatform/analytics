package org.exoplatform.addon.analytics.api.service;

import java.util.List;

import org.exoplatform.addon.analytics.model.*;

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

  public abstract ChartDataList getChartData(AnalyticsFilter filter);

  public abstract List<StatisticData> getData(AnalyticsFilter searchFilter);

  public abstract int count(AnalyticsFilter searchFilter);

}
