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

import java.util.*;

import org.junit.Test;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.chart.ChartData;
import org.exoplatform.analytics.model.chart.ChartDataList;
import org.exoplatform.analytics.model.es.FieldMapping;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsServiceTest extends BaseAnalyticsTest {
  private static final Log LOG = ExoLogger.getLogger(AnalyticsServiceTest.class);

  @Test
  public void testAnalyticsInjection() {
    try {
      assertFalse("Analytics data shouldn't be injected", analyticsDataInjector.isDataInjected());

      analyticsDataInjector.setEnabled(true);
      analyticsDataInjector.start();
      assertTrue("Analytics data should be injected", analyticsDataInjector.isDataInjected());

      processIndexingQueue();

      List<StatisticData> injectedDate = analyticsService.retrieveData(null);
      assertNotNull("Returned injected data is null", injectedDate);
      assertFalse("Returned injected data is empty", injectedDate.isEmpty());
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testRetrieveFieldsMapping() {
    try {
      analyticsDataInjector.reinjectData();

      processIndexingQueue();

      Set<FieldMapping> fieldsMappings = analyticsService.retrieveMapping(false);
      assertNotNull("Returned fields mapping is null", fieldsMappings);
      assertFalse("Returned fields mapping is empty", fieldsMappings.isEmpty());
      assertEquals("Returned fields mapping count is wrong", 18, fieldsMappings.size());
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testSearchAnalyticsData() {
    try {
      analyticsDataInjector.reinjectData();

      processIndexingQueue();

      List<StatisticData> injectedDate = analyticsService.retrieveData(null);
      assertNotNull("Returned injected data is null", injectedDate);
      assertFalse("Returned injected data is empty", injectedDate.isEmpty());

      AnalyticsFilter filter = new AnalyticsFilter();

      filter.addEqualFilter("activityId", "1");

      filter.addEqualFilter("module", "social");
      filter.addLessFilter("spaceId", 5);
      filter.addGreaterFilter("month", 6);
      filter.addRangeFilter("year", 2018, 2019);
      filter.addInSetFilter("dayOfMonth", "2", "3", "4");

      List<StatisticData> statisticsData = analyticsService.retrieveData(filter);
      assertEquals("Unexpected injected data size", 2, statisticsData.size());
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAnalyticsChart() {
    try {
      analyticsDataInjector.reinjectData();

      processIndexingQueue();

      List<StatisticData> injectedDate = analyticsService.retrieveData(null);
      assertNotNull("Returned injected data is null", injectedDate);
      assertFalse("Returned injected data is empty", injectedDate.isEmpty());

      AnalyticsFilter analyticsFilter = new AnalyticsFilter();
      analyticsFilter.addEqualFilter("activityId", "1");
      analyticsFilter.addInSetFilter("module", "no_module", "social");

      AnalyticsAggregation yearAggregation = new AnalyticsAggregation();
      yearAggregation.setField("year");
      yearAggregation.setType(AnalyticsAggregationType.COUNT);
      analyticsFilter.addXAxisAggregation(yearAggregation);
      AnalyticsAggregation monthAggregation = new AnalyticsAggregation();
      monthAggregation.setField("month");
      monthAggregation.setType(AnalyticsAggregationType.COUNT);
      analyticsFilter.addXAxisAggregation(monthAggregation);
      AnalyticsAggregation dayOfMonthAggregation = new AnalyticsAggregation();
      dayOfMonthAggregation.setField("dayOfMonth");
      dayOfMonthAggregation.setType(AnalyticsAggregationType.COUNT);
      analyticsFilter.addXAxisAggregation(dayOfMonthAggregation);

      ChartDataList chartDataList = analyticsService.compueChartData(analyticsFilter);
      assertNotNull("Unexpected empty charts data", chartDataList);
      assertNotNull("Unexpected empty charts data size", chartDataList.getCharts());
      assertEquals("Unexpected empty charts data size", 1, chartDataList.getCharts().size());

      ChartData chartData = chartDataList.getCharts().iterator().next();

      assertNotNull("Unexpected injected data size", chartData);
      assertEquals("Unexpected injected labels (X axis) data size", 46, chartDataList.getLabels().size());
      assertEquals("Unexpected injected values (Y axis) data size",
                   chartDataList.getLabels().size(),
                   chartData.getValues().size());
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAnalyticsMultipleCharts() {
    try {
      analyticsDataInjector.reinjectData();

      processIndexingQueue();

      List<StatisticData> injectedDate = analyticsService.retrieveData(null);
      assertNotNull("Returned injected data is null", injectedDate);
      assertFalse("Returned injected data is empty", injectedDate.isEmpty());

      AnalyticsFilter analyticsFilter = new AnalyticsFilter();
      analyticsFilter.addEqualFilter("activityId", "1");
      analyticsFilter.addInSetFilter("module", "no_module", "social");

      analyticsFilter.setMultipleChartsField("month");

      AnalyticsAggregation dayOfMonthAggregation = new AnalyticsAggregation();
      dayOfMonthAggregation.setField("dayOfMonth");
      dayOfMonthAggregation.setType(AnalyticsAggregationType.COUNT);
      analyticsFilter.addXAxisAggregation(dayOfMonthAggregation);

      ChartDataList chartDataList = analyticsService.compueChartData(analyticsFilter);

      assertNotNull("Unexpected empty charts data", chartDataList);
      Set<ChartData> charts = chartDataList.getCharts();
      assertNotNull("Unexpected empty charts data list", charts);
      assertEquals("Unexpected charts data size", 5, charts.size());
      assertEquals("Unexpected injected labels (X axis) data size", 21, chartDataList.getLabels().size());

      Iterator<ChartData> chartsIterator = charts.iterator();
      while (chartsIterator.hasNext()) {
        ChartData chartData = chartsIterator.next();
        assertEquals("Unexpected injected values (Y axis) data size",
                     chartDataList.getLabels().size(),
                     chartData.getValues().size());
      }
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAnalyticsMultipleChartsByInterval() {
    try {
      analyticsDataInjector.reinjectData();

      processIndexingQueue();

      List<StatisticData> injectedDate = analyticsService.retrieveData(null);
      assertNotNull("Returned injected data is null", injectedDate);
      assertFalse("Returned injected data is empty", injectedDate.isEmpty());

      AnalyticsFilter analyticsFilter = new AnalyticsFilter();

      analyticsFilter.setMultipleChartsField("module");

      AnalyticsAggregation monthIntervalAggregation = new AnalyticsAggregation();
      monthIntervalAggregation.setField("timestamp");
      monthIntervalAggregation.setType(AnalyticsAggregationType.DATE);
      monthIntervalAggregation.setInterval("month");
      analyticsFilter.addXAxisAggregation(monthIntervalAggregation);

      ChartDataList chartDataList = analyticsService.compueChartData(analyticsFilter);

      assertNotNull("Unexpected empty charts data", chartDataList);
      assertNotNull("Unexpected empty charts data list", chartDataList.getCharts());
      assertEquals("Unexpected charts data size", 6, chartDataList.getCharts().size());
      assertEquals("Unexpected injected labels (X axis) data size", 5, chartDataList.getLabels().size());

      Set<ChartData> charts = chartDataList.getCharts();
      Iterator<ChartData> chartsIterator = charts.iterator();
      while (chartsIterator.hasNext()) {
        ChartData chartData = chartsIterator.next();
        assertEquals("Unexpected injected values (Y axis) data size",
                     chartDataList.getLabels().size(),
                     chartData.getValues().size());
      }
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

}
