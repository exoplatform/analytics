package org.exoplatform.analytics.api.service;

import java.util.List;
import java.util.Set;

import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.chart.ChartDataList;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;

public interface AnalyticsService {

  /**
   * Retrieve analytics chart data
   * 
   * @param filter used search filters and aggregations to compute data
   * @return computed analytics chart data
   */
  ChartDataList compueChartData(AnalyticsFilter filter);

  /**
   * Retrieve data using search filters
   * 
   * @param searchFilter
   * @return {@link List} of {@link StatisticData}
   */
  List<StatisticData> retrieveData(AnalyticsFilter searchFilter);

  /**
   * @param forceRefresh whether force refresh from ES or not
   * @return a {@link Set} of ES mapping fields
   */
  Set<StatisticFieldMapping> retrieveMapping(boolean forceRefresh);

}
