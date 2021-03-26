package org.exoplatform.analytics.api.service;

import java.util.List;
import java.util.Set;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.chart.*;
import org.exoplatform.analytics.model.filter.*;

public interface AnalyticsService {

  /**
   * Retrieve analytics chart data
   * 
   * @param filter used search filters and aggregations to compute data
   * @return computed analytics chart data
   */
  ChartDataList computeChartData(AnalyticsFilter filter);

  /**
   * Retrieve analytics percentage chart data
   * 
   * @param filter used search filters and aggregations to compute data
   * @return computed analytics chart data
   */
  PercentageChartResult computePercentageChartData(AnalyticsPercentageFilter filter);

  /**
   * Retrieve analytics table data
   * 
   * @param tableFilter overall configured table filter
   * @param filter used search filters and aggregations to compute data
   * @param period selected {@link AnalyticsPeriod}
   * @param periodType selected {@link AnalyticsPeriodType}
   * @param columnIndex column index in the configured table filter
   * @return computed analytics table column data
   */
  TableColumnResult computeTableColumnData(AnalyticsTableFilter tableFilter,
                                           AnalyticsFilter filter,
                                           AnalyticsPeriod period,
                                           AnalyticsPeriodType periodType,
                                           int columnIndex);

  /**
   * Retrieve analytics percentage chart data
   * 
   * @param filter used search filters and aggregations to compute data
   * @param currentPeriod current period of time
   * @param previousPeriod previous period of time to compare with
   * @param hasLimitAggregation whether aggregations has limit aggregation or
   *          not
   * @return {@link PercentageChartValue} containing current and previous values
   *         and thresholds
   */
  PercentageChartValue computePercentageChartData(AnalyticsFilter filter,
                                                  AnalyticsPeriod currentPeriod,
                                                  AnalyticsPeriod previousPeriod,
                                                  boolean hasLimitAggregation);

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

  /**
   * @return the analytics permission expression
   */
  List<String> getAdministratorsPermissions();

  /**
   * @return the {@link List} of permission expression of users that can access
   *         all analytics datas
   */
  List<String> getViewAllPermissions();

  /**
   * @return the {@link List} of permission expression of users that can access
   *         their (and their spaces they are member of) analytics datas
   */
  List<String> getViewPermissions();

  /**
   * @return {@link List} of {@link StatisticWatcher} containing DOM selectors
   *         of elements to watch
   */
  List<StatisticWatcher> getUIWatchers();

  /**
   * @param name corresponding UI watcher name, see StatisticWatcher#getName()
   * @return {@link StatisticWatcher} having switch corresponding name
   */
  StatisticWatcher getUIWatcher(String name);

  /**
   * Add watcher plugin
   * 
   * @param uiWatcherPlugin Kernel component plugin to add
   */
  void addUIWatcherPlugin(StatisticUIWatcherPlugin uiWatcherPlugin);

}
