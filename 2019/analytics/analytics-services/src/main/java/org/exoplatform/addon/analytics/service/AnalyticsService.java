package org.exoplatform.addon.analytics.service;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.ANALYTICS_NEW_DATA_EVENT;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.addon.analytics.api.StatisticDataProcessor;
import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.addon.analytics.model.chart.ChartType;
import org.exoplatform.addon.analytics.model.chart.LineChartData;
import org.exoplatform.services.listener.ListenerService;

public class AnalyticsService {

  private List<StatisticDataProcessor> processors      = new ArrayList<>();

  private ListenerService              listenerService = null;

  public AnalyticsService(ListenerService listenerService) {
    this.listenerService = listenerService;
  }

  public LineChartData getChartData(ChartType chartType) {
    // TODO Auto-generated method stub
    return null;
  }

  public void save(StatisticData data) {
    if (data == null) {
      throw new IllegalArgumentException("Statistic data to store is mandatory");
    }
    try {
      listenerService.broadcast(ANALYTICS_NEW_DATA_EVENT, this, data);
    } catch (Exception e) {
      throw new IllegalStateException("Error processing new analytics data: " + data, e);
    }
  }

  public void registerProcessor(StatisticDataProcessor statisticDataProcessor) {
    processors.add(statisticDataProcessor);
  }

  public List<StatisticDataProcessor> getProcessors() {
    return processors;
  }
}
