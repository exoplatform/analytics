package org.exoplatform.analytics.model.filter;

import static org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType.RANGE;

import java.io.Serializable;
import java.time.*;
import java.util.*;

import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPercentageFilter implements Serializable, Cloneable {

  private static final long             serialVersionUID = 5699550622069979910L;

  private String                        title;

  private String                        chartType;

  private List<String>                  colors;

  private AnalyticsFieldFilter          scopeFilter;

  private String                        periodType;

  private LocalDate                     periodDate;

  private AnalyticsPercentageItemFilter value;

  private AnalyticsPercentageItemFilter threshold;

  private String                        lang             = null;

  // TODO to delete if not used in second chart
  private long                          offset           = 0;

  // TODO to delete if not used in second chart
  private long                          limit            = 0;

  public AnalyticsAggregation computeXAxisAggregation() {
    AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
    if (analyticsPeriodType == null) {
      return null;
    }
    AnalyticsAggregation xAxisAggregation = new AnalyticsAggregation();
    xAxisAggregation.setField("timestamp");
    xAxisAggregation.setSortDirection("DESC");
    xAxisAggregation.setType(AnalyticsAggregationType.DATE);
    xAxisAggregation.setInterval(analyticsPeriodType.getInterval());
    return xAxisAggregation;
  }

  public AnalyticsPeriodType getAnalyticsPeriodType() {
    return AnalyticsPeriodType.periodTypeByName(periodType);
  }

  public AnalyticsPeriod getCurrentAnalyticsPeriod() {
    AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
    if (analyticsPeriodType == null || periodDate == null) {
      return null;
    }
    return analyticsPeriodType.getCurrentPeriod(periodDate);
  }

  public AnalyticsPeriod getPreviousAnalyticsPeriod() {
    AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
    if (analyticsPeriodType == null || periodDate == null) {
      return null;
    }
    return analyticsPeriodType.getPreviousPeriod(periodDate);
  }

  public AnalyticsAggregation computeValueYAggregations() {
    return value == null ? null : value.getYAxisAggregation();
  }

  public AnalyticsAggregation computeThresholdYAggregations() {
    return threshold == null ? null : threshold.getYAxisAggregation();
  }

  public List<AnalyticsFieldFilter> computeValueFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    AnalyticsFieldFilter periodFilter = computePeriodFilter();
    if (periodFilter != null) {
      filters.add(computePeriodFilter());
    }
    if (scopeFilter != null) {
      filters.add(scopeFilter);
    }
    if (value != null && value.getFilters() != null) {
      filters.addAll(value.getFilters());
    }
    return filters;
  }

  public List<AnalyticsFieldFilter> computeThresholdFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    AnalyticsFieldFilter periodFilter = computePeriodFilter();
    if (periodFilter != null) {
      filters.add(periodFilter);
    }
    if (scopeFilter != null) {
      filters.add(scopeFilter);
    }
    if (threshold != null && threshold.getFilters() != null) {
      filters.addAll(threshold.getFilters());
    }
    return filters;
  }

  public List<AnalyticsAggregation> computeValueAggregations() {
    List<AnalyticsAggregation> aggregations = new ArrayList<>();

    AnalyticsAggregation xAxisAggregation = computeXAxisAggregation();
    if (xAxisAggregation != null) {
      aggregations.add(xAxisAggregation);
    }
    if (value != null && value.getYAxisAggregation() != null) {
      aggregations.add(value.getYAxisAggregation());
    }
    return aggregations;
  }

  public List<AnalyticsAggregation> computeThresholdAggregations() {
    List<AnalyticsAggregation> aggregations = new ArrayList<>();

    AnalyticsAggregation xAxisAggregation = computeXAxisAggregation();
    if (xAxisAggregation != null) {
      aggregations.add(xAxisAggregation);
    }
    if (threshold != null && threshold.getYAxisAggregation() != null) {
      aggregations.add(threshold.getYAxisAggregation());
    }
    return aggregations;
  }

  public void setPeriodDateInMS(long timestampInMS) {
    periodDate = Instant.ofEpochMilli(timestampInMS).atZone(ZoneOffset.UTC).toLocalDate();
  }

  public AnalyticsFilter computeValueFilter() {
    AnalyticsAggregation xAxisAggregation = computeXAxisAggregation();
    AnalyticsAggregation valueYAggregations = computeValueYAggregations();
    List<AnalyticsFieldFilter> valueFilters = computeValueFilters();
    return new AnalyticsFilter(title,
                               chartType,
                               colors,
                               valueFilters == null ? Collections.emptyList() : valueFilters,
                               null,
                               xAxisAggregation == null ? Collections.emptyList() : Collections.singletonList(xAxisAggregation),
                               valueYAggregations == null ? null : valueYAggregations,
                               lang,
                               offset,
                               limit);
  }

  public AnalyticsFilter computeThresholdFilter() {
    AnalyticsAggregation xAxisAggregation = computeXAxisAggregation();
    AnalyticsAggregation thresholdYAggregations = computeThresholdYAggregations();
    List<AnalyticsFieldFilter> thresholdFilters = computeThresholdFilters();
    return new AnalyticsFilter(title,
                               chartType,
                               colors,
                               thresholdFilters == null ? Collections.emptyList() : thresholdFilters,
                               null,
                               xAxisAggregation == null ? Collections.emptyList() : Collections.singletonList(xAxisAggregation),
                               thresholdYAggregations == null ? null : thresholdYAggregations,
                               lang,
                               offset,
                               limit);
  }

  @Override
  public AnalyticsPercentageFilter clone() { // NOSONAR
    LocalDate clonedPeriodDate = periodDate == null ? null : LocalDate.from(periodDate);
    AnalyticsFieldFilter clonedScopeFilter = scopeFilter == null ? null : scopeFilter.clone();
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterValue = value == null ? null : value.clone();
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterThreshold = threshold == null ? null
                                                                                                  : threshold.clone();

    return new AnalyticsPercentageFilter(title,
                                         chartType,
                                         colors,
                                         clonedScopeFilter,
                                         periodType,
                                         clonedPeriodDate,
                                         cloneAnalyticsPercentageItemFilterValue,
                                         cloneAnalyticsPercentageItemFilterThreshold,
                                         lang,
                                         offset,
                                         limit);
  }

  private AnalyticsFieldFilter computePeriodFilter() {
    AnalyticsPeriod currentAnalyticsPeriod = getCurrentAnalyticsPeriod();
    AnalyticsPeriod previousAnalyticsPeriod = getPreviousAnalyticsPeriod();
    if (previousAnalyticsPeriod == null || currentAnalyticsPeriod == null) {
      return null;
    }
    AnalyticsFilter.Range rangeFilter = new AnalyticsFilter.Range(previousAnalyticsPeriod.getFromInMS(),
                                                                  currentAnalyticsPeriod.getToInMS());
    return new AnalyticsFieldFilter("timestamp", RANGE, rangeFilter);
  }

}
