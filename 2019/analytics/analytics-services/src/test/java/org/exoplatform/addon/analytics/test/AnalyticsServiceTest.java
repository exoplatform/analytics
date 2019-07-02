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
package org.exoplatform.addon.analytics.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.exoplatform.addon.analytics.model.*;
import org.exoplatform.addon.analytics.model.aggregation.AnalyticsAggregation;
import org.exoplatform.addon.analytics.model.aggregation.AnalyticsAggregationType;
import org.exoplatform.addon.analytics.model.search.AnalyticsSearchFilter;

public class AnalyticsServiceTest extends BaseAnalyticsTest {

  @Test
  public void testServicesStarted() {
    assertNotNull("Analytics Service is not instantiated", analyticsService);
    assertNotNull("Analytics Queue Service is not instantiated", analyticsQueueService);
    assertNotNull("Analytics Data Injector Service is not instantiated", analyticsDataInjector);

    assertNotNull("Empty analytics queue consumers", analyticsQueueService.getProcessors());
    assertEquals("Unexpected number of processors", 1, analyticsQueueService.getProcessors().size());
  }

  @Test
  public void testAnalyticsInjection() throws InterruptedException {
    assertFalse("Analytics data shouldn't be injected", analyticsDataInjector.isDataInjected());

    analyticsDataInjector.setEnabled(true);
    analyticsDataInjector.start();
    assertTrue("Analytics data should be injected", analyticsDataInjector.isDataInjected());

    processIndexingQueue();

    assertEquals("Unexpected injected data size", 1633, analyticsService.count(null));

    List<StatisticData> injectedDate = analyticsService.getData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());
  }

  @Test
  public void testSearchAnalyticsData() throws InterruptedException {
    analyticsDataInjector.reinjectData();

    processIndexingQueue();

    assertEquals("Unexpected injected data size", 1633, analyticsService.count(null));

    List<StatisticData> injectedDate = analyticsService.getData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsSearchFilter filter = new AnalyticsSearchFilter();
    assertEquals("Unexpected injected data size", 1633, analyticsService.count(filter));

    filter.addEqualFilter("activityId", "1");
    assertEquals("Unexpected injected data size", 1633, analyticsService.count(filter));

    filter.addEqualFilter("module", "social");
    filter.addLessFilter("spaceId", 5);
    filter.addGreaterFilter("month", 6);
    filter.addRangeFilter("year", 2018, 2019);
    filter.addInSetFilter("dayOfMonth", "2", "3", "4");

    assertEquals("Unexpected injected data size", 27, analyticsService.count(filter));
  }

  @Test
  public void testGetAnalyticsChart() throws InterruptedException {
    analyticsDataInjector.reinjectData();

    processIndexingQueue();

    assertEquals("Unexpected injected data size", 1633, analyticsService.count(null));

    List<StatisticData> injectedDate = analyticsService.getData(null);
    assertNotNull("Returned injected data is null", injectedDate);
    assertFalse("Returned injected data is empty", injectedDate.isEmpty());

    AnalyticsFilter analyticsFilter = new AnalyticsFilter();
    AnalyticsSearchFilter filter = analyticsFilter.getSearchFilter();
    List<AnalyticsAggregation> aggregations = analyticsFilter.getAggregations();

    filter.addEqualFilter("activityId", "1");
    filter.addInSetFilter("module", "no_module", "social");

    AnalyticsAggregation yearAggregation = new AnalyticsAggregation();
    yearAggregation.setField("year");
    yearAggregation.setType(AnalyticsAggregationType.COUNT);
    aggregations.add(yearAggregation);
    AnalyticsAggregation monthAggregation = new AnalyticsAggregation();
    monthAggregation.setField("month");
    monthAggregation.setType(AnalyticsAggregationType.COUNT);
    aggregations.add(monthAggregation);
    AnalyticsAggregation dayOfMonthAggregation = new AnalyticsAggregation();
    dayOfMonthAggregation.setField("dayOfMonth");
    dayOfMonthAggregation.setType(AnalyticsAggregationType.COUNT);
    aggregations.add(dayOfMonthAggregation);

    ChartData chartData = analyticsService.getChartData(analyticsFilter);

    assertNotNull("Unexpected injected data size", chartData);
    assertEquals("Unexpected injected labels (X axis) data size", 40, chartData.getChartXLabels().size());
    assertEquals("Unexpected injected values (Y axis) data size",
                 chartData.getChartXLabels().size(),
                 chartData.getChartYData().size());
  }

}
