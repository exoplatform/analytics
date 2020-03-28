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
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.chart.ChartData;
import org.exoplatform.analytics.model.chart.ChartDataList;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsServiceTestIT extends BaseAnalyticsTest {

  private static final Log LOG = ExoLogger.getLogger(AnalyticsServiceTestIT.class);

  @Test
  public void testAnalyticsInjection() {
    try {
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
      Set<StatisticFieldMapping> fieldsMappings = analyticsService.retrieveMapping(true);
      assertNotNull("Returned fields mapping is null", fieldsMappings);
      assertFalse("Returned fields mapping is empty", fieldsMappings.isEmpty());
      assertTrue("Returned fields mapping count is wrong, should be at least 11 as configured", fieldsMappings.size() >= 11);
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testSearchAnalyticsData() {
    try {
      List<StatisticData> injectedDate = analyticsService.retrieveData(null);
      assertNotNull("Returned injected data is null", injectedDate);
      assertFalse("Returned injected data is empty", injectedDate.isEmpty());

      AnalyticsFilter filter = new AnalyticsFilter();

      filter.addEqualFilter("module", "portal");
      filter.addEqualFilter("subModule", "login");
      filter.addLessFilter("userId", 23);
      filter.addRangeFilter("timestamp", "1574246024553", "1575775885451");
      filter.addInSetFilter("operation", "login", "logout");

      List<StatisticData> statisticsData = analyticsService.retrieveData(filter);
      assertEquals("Unexpected injected data size", 14, statisticsData.size());
    } catch (Exception e) {
      LOG.error("Error occurred in test", e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAnalyticsChart() {
    try {
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
      assertEquals("Unexpected injected labels (X axis) data size", 4, chartDataList.getLabels().size());
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
      assertEquals("Unexpected injected labels (X axis) data size", 2, chartDataList.getLabels().size());

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
      assertEquals("Unexpected charts data size", 2, chartDataList.getCharts().size());
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
