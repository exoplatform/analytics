package org.exoplatform.addon.analytics.api.service;

import java.util.List;

import org.exoplatform.addon.analytics.model.AnalyticsFilter;
import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.addon.analytics.model.chart.ChartType;
import org.exoplatform.addon.analytics.model.chart.LineChartData;

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

  public abstract LineChartData getChartData(ChartType chartType);

  public abstract List<StatisticData> getData(AnalyticsFilter filter);

  public abstract int count(AnalyticsFilter filter);

}
