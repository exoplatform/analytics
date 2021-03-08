package org.exoplatform.analytics.api.service;

import java.util.List;
import java.util.Set;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.chart.ChartDataList;
import org.exoplatform.analytics.model.chart.PercentageChartResult;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.analytics.model.filter.AnalyticsPercentageFilter;

public interface AnalyticsService {

  /**
   * Retrieve analytics chart data
   * 
   * @param filter used search filters and aggregations to compute data
   * @return computed analytics chart data
   */
  ChartDataList computeChartData(AnalyticsFilter filter);

  /**
   * Retrieve analytics chart data
   * 
   * @param filter used search filters and aggregations to compute data
   * @return computed analytics chart data
   */
  PercentageChartResult computeChartData(AnalyticsPercentageFilter filter);

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
