package org.exoplatform.analytics.model.filter;

import static org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType.*;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;
import org.exoplatform.analytics.utils.AnalyticsUtils;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
public class AnalyticsFilter implements Serializable {

  private static final long          serialVersionUID    = 5699550622069979910L;

  private String                     title;

  private String                     chartType;

  private boolean                    displayComputingTime;

  private boolean                    displaySamplesCount;

  private List<AnalyticsFieldFilter> filters             = new ArrayList<>();

  private String                     multipleChartsField = null;

  private List<AnalyticsAggregation> xAxisAggregations   = new ArrayList<>();

  private AnalyticsAggregation       yAxisAggregation    = null;

  private String                     lang                = null;

  private long                       offset              = 0;

  private long                       limit               = 0;

  public List<AnalyticsAggregation> getAggregations() {
    List<AnalyticsAggregation> aggregations = new ArrayList<>();

    AnalyticsAggregation multipleChartsAggregation = getMultipleChartsAggregation();
    if (multipleChartsAggregation != null) {
      aggregations.add(multipleChartsAggregation);
    }

    aggregations.addAll(xAxisAggregations);

    if (yAxisAggregation != null && StringUtils.isNotBlank(yAxisAggregation.getField())) {
      aggregations.add(yAxisAggregation);
    }

    return Collections.unmodifiableList(aggregations);
  }

  public AnalyticsAggregation getMultipleChartsAggregation() {
    if (isMultipleCharts()) {
      return new AnalyticsAggregation(multipleChartsField);
    }
    return null;
  }

  public void addXAxisAggregation(AnalyticsAggregation aggregation) {
    xAxisAggregations.add(aggregation);
  }

  public boolean isMultipleCharts() {
    return StringUtils.isNotBlank(multipleChartsField);
  }

  public void addEqualFilter(String field, String value) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, EQUAL, String.valueOf(value));
    this.filters.add(fieldFilter);
  }

  public void addInSetFilter(String field, String... values) {
    if (values != null && values.length > 0) {
      AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field,
                                                                  IN_SET,
                                                                  StringUtils.join(values, AnalyticsUtils.VALUES_SEPARATOR));
      this.filters.add(fieldFilter);
    }
  }

  public void addRangeFilter(String field, String start, String end) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field,
                                                                RANGE,
                                                                new Range(start, end));
    this.filters.add(fieldFilter);
  }

  public void addGreaterFilter(String field, long value) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, GREATER, String.valueOf(value));
    this.filters.add(fieldFilter);
  }

  public void addLessFilter(String field, long value) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, LESS, String.valueOf(value));
    this.filters.add(fieldFilter);
  }

  @Data
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Range implements Serializable {

    private static final long serialVersionUID = 570632355720481459L;

    private String            min;

    private String            max;

  }
}
