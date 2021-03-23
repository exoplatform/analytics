/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.analytics.test;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import org.junit.Test;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.chart.*;
import org.exoplatform.analytics.model.filter.*;
import org.exoplatform.analytics.model.filter.aggregation.*;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType;

public class AnalyticsServiceTestIT extends BaseAnalyticsTest {

  @Test
  public void testAnalyticsInjection() {
    List<StatisticData> injectedDate = analyticsService.retrieveData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());
  }

  @Test
  public void testRetrieveFieldsMapping() {
    Set<StatisticFieldMapping> fieldsMappings = analyticsService.retrieveMapping(true);
    assertNotNull("Returned fields mapping is null", fieldsMappings);
    assertFalse("Returned fields mapping is empty", fieldsMappings.isEmpty());
    assertTrue("Returned fields mapping count is wrong, should be at least 11 as configured", fieldsMappings.size() >= 11);
  }

  @Test
  public void testSearchAnalyticsData() {
    List<StatisticData> injectedDate = analyticsService.retrieveData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsFilter filter = new AnalyticsFilter();

    filter.addEqualFilter("module", "portal");
    filter.addEqualFilter("subModule", "login");
    filter.addLessFilter("userId", 23);
    filter.addRangeFilter("timestamp", "1574246024552", "1575775885452");
    filter.addInSetFilter("operation", "login", "logout");

    List<StatisticData> statisticsData = analyticsService.retrieveData(filter);
    assertEquals("Unexpected injected data size", 388, statisticsData.size());
  }

  @Test
  public void testGetAnalyticsChart() {
    List<StatisticData> injectedDate = analyticsService.retrieveData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsFilter analyticsFilter = new AnalyticsFilter();
    analyticsFilter.addEqualFilter("module", "portal");
    analyticsFilter.addNotEqualFilter("module", "social");
    analyticsFilter.addInSetFilter("subModule", "no_module", "login");

    AnalyticsAggregation operationAggregation = new AnalyticsAggregation();
    operationAggregation.setField("operation");
    operationAggregation.setType(AnalyticsAggregationType.COUNT);
    analyticsFilter.addXAxisAggregation(operationAggregation);
    AnalyticsAggregation userIdAggregation = new AnalyticsAggregation();
    userIdAggregation.setField("userId");
    userIdAggregation.setType(AnalyticsAggregationType.COUNT);
    analyticsFilter.addXAxisAggregation(userIdAggregation);
    AnalyticsAggregation statusAggregation = new AnalyticsAggregation();
    statusAggregation.setField("status");
    statusAggregation.setType(AnalyticsAggregationType.COUNT);
    analyticsFilter.addXAxisAggregation(statusAggregation);

    ChartDataList chartDataList = analyticsService.computeChartData(analyticsFilter);
    assertNotNull("Unexpected empty charts data", chartDataList);
    assertNotNull("Unexpected empty charts data size", chartDataList.getCharts());
    assertEquals("Unexpected empty charts data size", 1, chartDataList.getCharts().size());

    ChartData chartData = chartDataList.getCharts().iterator().next();

    assertNotNull("Unexpected injected data size", chartData);
    assertEquals("Unexpected injected labels (X axis) data size", 66, chartDataList.getLabels().size());
    assertEquals("Unexpected injected values (Y axis) data size",
                 chartDataList.getLabels().size(),
                 chartData.getValues().size());
  }

  @Test
  public void testGetAnalyticsChartByLimit() {
    List<StatisticData> injectedDate = analyticsService.retrieveData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsFilter analyticsFilter = new AnalyticsFilter();
    analyticsFilter.addEqualFilter("module", "portal");
    analyticsFilter.addNotEqualFilter("module", "social");
    analyticsFilter.addInSetFilter("subModule", "no_module", "login", "logout");

    AnalyticsAggregation operationAggregation = new AnalyticsAggregation();
    operationAggregation.setField("operation");
    operationAggregation.setType(AnalyticsAggregationType.TERMS);
    analyticsFilter.addXAxisAggregation(operationAggregation);
    AnalyticsAggregation userIdAggregation = new AnalyticsAggregation();
    userIdAggregation.setField("userId");
    userIdAggregation.setType(AnalyticsAggregationType.TERMS);
    userIdAggregation.setLimit(20);
    analyticsFilter.addXAxisAggregation(userIdAggregation);
    AnalyticsAggregation statusAggregation = new AnalyticsAggregation();
    statusAggregation.setField("subModule");
    statusAggregation.setType(AnalyticsAggregationType.TERMS);
    analyticsFilter.addXAxisAggregation(statusAggregation);

    ChartDataList chartDataList = analyticsService.computeChartData(analyticsFilter);
    assertNotNull("Unexpected empty charts data", chartDataList);
    assertNotNull("Unexpected empty charts data size", chartDataList.getCharts());
    assertEquals("Unexpected empty charts data size", 1, chartDataList.getCharts().size());

    ChartData chartData = chartDataList.getCharts().iterator().next();

    assertNotNull("Unexpected injected data size", chartData);
    assertEquals("Unexpected injected labels (X axis) data size", 40, chartDataList.getLabels().size());
    assertEquals("Unexpected injected values (Y axis) data size",
                 chartDataList.getLabels().size(),
                 chartData.getValues().size());
  }

  @Test
  public void testGetAnalyticsChartBySort() {
    List<StatisticData> injectedDate = analyticsService.retrieveData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsFilter analyticsFilter = new AnalyticsFilter();
    analyticsFilter.addEqualFilter("module", "portal");
    analyticsFilter.addNotEqualFilter("module", "social");
    analyticsFilter.addInSetFilter("subModule", "no_module", "login", "logout");

    AnalyticsAggregation operationAggregation = new AnalyticsAggregation();
    operationAggregation.setField("operation");
    operationAggregation.setType(AnalyticsAggregationType.TERMS);
    operationAggregation.setLimit(1);
    operationAggregation.setSortDirection("asc");
    analyticsFilter.addXAxisAggregation(operationAggregation);
    AnalyticsAggregation userIdAggregation = new AnalyticsAggregation();
    userIdAggregation.setField("userId");
    userIdAggregation.setType(AnalyticsAggregationType.TERMS);
    userIdAggregation.setLimit(1);
    userIdAggregation.setSortDirection("asc");
    analyticsFilter.addXAxisAggregation(userIdAggregation);
    AnalyticsAggregation statusAggregation = new AnalyticsAggregation();
    statusAggregation.setField("subModule");
    statusAggregation.setType(AnalyticsAggregationType.TERMS);
    statusAggregation.setLimit(1);
    statusAggregation.setSortDirection("asc");
    analyticsFilter.addXAxisAggregation(statusAggregation);

    ChartDataList chartDataList = analyticsService.computeChartData(analyticsFilter);
    assertNotNull("Unexpected empty charts data", chartDataList);
    assertNotNull("Unexpected empty charts data size", chartDataList.getCharts());
    assertEquals("Unexpected empty charts data size", 1, chartDataList.getCharts().size());

    ChartData chartData = chartDataList.getCharts().iterator().next();

    assertNotNull(chartData);
    assertNotNull(chartData.getAggregationResults());

    assertEquals("Unexpected aggregation results size",
                 1,
                 chartData.getAggregationResults().size());

    assertEquals("Unexpected aggregation result",
                 "operation=login-userId=42-subModule=login",
                 chartData.getAggregationResults().get(0).getLabel());

    assertEquals("Unexpected aggregation results size",
                 "15",
                 chartData.getAggregationResults().get(0).getValue());

    operationAggregation.setSortDirection("desc");
    userIdAggregation.setSortDirection("desc");
    statusAggregation.setSortDirection("desc");
    chartDataList = analyticsService.computeChartData(analyticsFilter);
    chartData = chartDataList.getCharts().iterator().next();

    assertNotNull(chartData);
    assertNotNull(chartData.getAggregationResults());

    assertEquals("Unexpected aggregation results size",
                 1,
                 chartData.getAggregationResults().size());

    assertEquals("Unexpected aggregation result",
                 "operation=logout-userId=11-subModule=login",
                 chartData.getAggregationResults().get(0).getLabel());

    assertEquals("Unexpected aggregation results size",
                 "92",
                 chartData.getAggregationResults().get(0).getValue());
  }

  @Test
  public void testGetAnalyticsMultipleCharts() {
    List<StatisticData> injectedDate = analyticsService.retrieveData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsFilter analyticsFilter = new AnalyticsFilter();
    analyticsFilter.addInSetFilter("module", "no_module", "portal");

    analyticsFilter.setMultipleChartsField("operation");

    AnalyticsAggregation userIdAggregation = new AnalyticsAggregation();
    userIdAggregation.setField("userId");
    userIdAggregation.setType(AnalyticsAggregationType.COUNT);
    analyticsFilter.addXAxisAggregation(userIdAggregation);
    analyticsFilter.addNotInSetFilter("userId", "5366, 9999");

    ChartDataList chartDataList = analyticsService.computeChartData(analyticsFilter);

    assertNotNull("Unexpected empty charts data", chartDataList);
    Set<ChartData> charts = chartDataList.getCharts();
    assertNotNull("Unexpected empty charts data list", charts);
    assertEquals("Unexpected charts data size", 2, charts.size());
    assertEquals("Unexpected injected labels (X axis) data size", 33, chartDataList.getLabels().size());

    Iterator<ChartData> chartsIterator = charts.iterator();
    while (chartsIterator.hasNext()) {
      ChartData chartData = chartsIterator.next();
      assertEquals("Unexpected injected values (Y axis) data size",
                   chartDataList.getLabels().size(),
                   chartData.getValues().size());
    }
  }

  @Test
  public void testGetAnalyticsMultipleChartsByInterval() {
    List<StatisticData> injectedDate = analyticsService.retrieveData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsFilter analyticsFilter = new AnalyticsFilter();

    analyticsFilter.setMultipleChartsField("userId");

    AnalyticsAggregation monthIntervalAggregation = new AnalyticsAggregation();
    monthIntervalAggregation.setField("timestamp");
    monthIntervalAggregation.setType(AnalyticsAggregationType.DATE);
    monthIntervalAggregation.setInterval("month");
    analyticsFilter.addXAxisAggregation(monthIntervalAggregation);

    ChartDataList chartDataList = analyticsService.computeChartData(analyticsFilter);

    assertNotNull("Unexpected empty charts data", chartDataList);
    assertNotNull("Unexpected empty charts data list", chartDataList.getCharts());
    assertEquals("Unexpected charts data size", 33, chartDataList.getCharts().size());
    assertEquals("Unexpected injected labels (X axis) data size", 5, chartDataList.getLabels().size());

    Set<ChartData> charts = chartDataList.getCharts();
    Iterator<ChartData> chartsIterator = charts.iterator();
    while (chartsIterator.hasNext()) {
      ChartData chartData = chartsIterator.next();
      assertEquals("Unexpected injected values (Y axis) data size",
                   chartDataList.getLabels().size(),
                   chartData.getValues().size());
    }
  }

  @Test
  public void testGetAnalyticsPercentage() {
    AnalyticsPercentageFilter filter = new AnalyticsPercentageFilter();
    filter.setChartType("percentageBar");
    filter.setScopeFilter(null);
    AnalyticsPercentageItemFilter valueFilter = new AnalyticsPercentageItemFilter();
    filter.setValue(valueFilter);
    AnalyticsPercentageItemFilter thresholdFilter = new AnalyticsPercentageItemFilter();
    filter.setThreshold(thresholdFilter);

    // compute percentage of login of a user (id = 19) comparing to all logins
    // in the platform
    valueFilter.addEqualFilter("subModule", "login");
    valueFilter.addEqualFilter("userId", "19");
    thresholdFilter.addEqualFilter("subModule", "login");

    filter.setPeriodType(AnalyticsPeriodType.THIS_MONTH.getTypeName());
    filter.setPeriodDateInMS(LocalDate.of(2019, 12, 01)
                                      .atStartOfDay(ZoneOffset.UTC)
                                      .toInstant()
                                      .toEpochMilli());

    AnalyticsAggregation yAxisAggregation = new AnalyticsAggregation(AnalyticsAggregationType.COUNT, null, "desc", "1d", 0);
    valueFilter.setYAxisAggregation(yAxisAggregation);
    thresholdFilter.setYAxisAggregation(yAxisAggregation);

    PercentageChartResult percentageChartDataList = analyticsService.computeChartData(filter);
    assertNotNull(percentageChartDataList);
    assertEquals(48d, percentageChartDataList.getCurrentPeriodValue(), 0);
    assertEquals(1691d, percentageChartDataList.getCurrentPeriodThreshold(), 0);
    assertEquals(32d, percentageChartDataList.getPreviousPeriodValue(), 0);
    assertEquals(985d, percentageChartDataList.getPreviousPeriodThreshold(), 0);

    filter.setPeriodType(AnalyticsPeriodType.THIS_QUARTER.getTypeName());
    filter.setPeriodDateInMS(LocalDate.of(2019, 12, 01)
                                      .atStartOfDay(ZoneOffset.UTC)
                                      .toInstant()
                                      .toEpochMilli());
    percentageChartDataList = analyticsService.computeChartData(filter);
    assertNotNull(percentageChartDataList);
    assertEquals(80l, percentageChartDataList.getCurrentPeriodValue(), 0);
    assertEquals(0, percentageChartDataList.getPreviousPeriodValue(), 0);
    assertEquals(2676d, percentageChartDataList.getCurrentPeriodThreshold(), 0);
    assertEquals(0, percentageChartDataList.getPreviousPeriodThreshold(), 0);

    filter.setPeriodType(AnalyticsPeriodType.THIS_QUARTER.getTypeName());
    filter.setPeriodDateInMS(LocalDate.of(2020, 6, 01)
                                      .atStartOfDay(ZoneOffset.UTC)
                                      .toInstant()
                                      .toEpochMilli());
    percentageChartDataList = analyticsService.computeChartData(filter);
    assertNotNull(percentageChartDataList);
    assertEquals(0, percentageChartDataList.getCurrentPeriodValue(), 0);
    assertEquals(103d, percentageChartDataList.getPreviousPeriodValue(), 0);
    assertEquals(0, percentageChartDataList.getCurrentPeriodThreshold(), 0);
    assertEquals(3483d, percentageChartDataList.getPreviousPeriodThreshold(), 0);

    AnalyticsFieldFilter spaceScopeFilter = new AnalyticsFieldFilter("spaceId", AnalyticsFieldFilterType.EQUAL, "2");
    filter.setScopeFilter(spaceScopeFilter);
    percentageChartDataList = analyticsService.computeChartData(filter);
    assertNotNull(percentageChartDataList);
    assertEquals(0, percentageChartDataList.getCurrentPeriodValue(), 0);
    assertEquals(0, percentageChartDataList.getPreviousPeriodValue(), 0);
    assertEquals(0, percentageChartDataList.getCurrentPeriodThreshold(), 0);
    assertEquals(0, percentageChartDataList.getPreviousPeriodThreshold(), 0);

    AnalyticsFieldFilter userScopeFilter = new AnalyticsFieldFilter("userId", AnalyticsFieldFilterType.EQUAL, "18");
    filter.setScopeFilter(userScopeFilter);
    percentageChartDataList = analyticsService.computeChartData(filter);
    assertNotNull(percentageChartDataList);
    assertEquals(0, percentageChartDataList.getCurrentPeriodValue(), 0);
    assertEquals(0, percentageChartDataList.getPreviousPeriodValue(), 0);
    assertEquals(0, percentageChartDataList.getCurrentPeriodThreshold(), 0);
    assertEquals(102d, percentageChartDataList.getPreviousPeriodThreshold(), 0);

    userScopeFilter = new AnalyticsFieldFilter("userId", AnalyticsFieldFilterType.EQUAL, "19");
    filter.setScopeFilter(userScopeFilter);
    percentageChartDataList = analyticsService.computeChartData(filter);
    assertNotNull(percentageChartDataList);
    assertEquals(0, percentageChartDataList.getCurrentPeriodValue(), 0);
    assertEquals(103d, percentageChartDataList.getPreviousPeriodValue(), 0);
    assertEquals(0, percentageChartDataList.getCurrentPeriodThreshold(), 0);
    assertEquals(103d, percentageChartDataList.getPreviousPeriodThreshold(), 0);
  }

  @Test
  public void testGetAnalyticsPercentageWithLimit() {
    AnalyticsPercentageFilter filter = new AnalyticsPercentageFilter();
    filter.setChartType("percentage");
    filter.setScopeFilter(null);

    AnalyticsPercentageItemFilter valueFilter = new AnalyticsPercentageItemFilter();
    valueFilter.addEqualFilter("subModule", "login");
    filter.setValue(valueFilter);

    AnalyticsPercentageItemFilter thresholdFilter = new AnalyticsPercentageItemFilter();
    thresholdFilter.addEqualFilter("subModule", "login");
    filter.setThreshold(thresholdFilter);

    AnalyticsPercentageLimit limitPercentageFilter = new AnalyticsPercentageLimit();
    limitPercentageFilter.setPercentage(50);
    limitPercentageFilter.setField("userId");
    limitPercentageFilter.setAggregation(new AnalyticsPercentageItemFilter());
    limitPercentageFilter.getAggregation().addEqualFilter("subModule", "login");
    AnalyticsAggregation limitYAxisAggregation = new AnalyticsAggregation(AnalyticsAggregationType.CARDINALITY,
                                                                          "userId",
                                                                          "desc",
                                                                          "1d",
                                                                          0);
    limitPercentageFilter.getAggregation().setYAxisAggregation(limitYAxisAggregation);
    filter.setPercentageLimit(limitPercentageFilter);

    filter.setPeriodType(AnalyticsPeriodType.THIS_MONTH.getTypeName());
    filter.setPeriodDateInMS(LocalDate.of(2019, 12, 01)
                                      .atStartOfDay(ZoneOffset.UTC)
                                      .toInstant()
                                      .toEpochMilli());

    AnalyticsAggregation yAxisAggregation = new AnalyticsAggregation(AnalyticsAggregationType.COUNT, null, "desc", "1d", 0);
    valueFilter.setYAxisAggregation(yAxisAggregation);
    thresholdFilter.setYAxisAggregation(yAxisAggregation);
    thresholdFilter.setYAxisAggregation(yAxisAggregation);

    PercentageChartResult percentageChartDataList = analyticsService.computeChartData(filter);
    assertNotNull(percentageChartDataList);
    assertEquals(950d, percentageChartDataList.getCurrentPeriodValue(), 0);
    assertEquals(1691d, percentageChartDataList.getCurrentPeriodThreshold(), 0);
    assertEquals(580d, percentageChartDataList.getPreviousPeriodValue(), 0);
    assertEquals(985d, percentageChartDataList.getPreviousPeriodThreshold(), 0);
  }
}
