package org.exoplatform.analytics.model.filter;

import static org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType.RANGE;

import java.io.Serializable;
import java.time.*;
import java.time.temporal.ChronoUnit;
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

  private LocalDate                     fromDate;

  private LocalDate                     toDate;

  private AnalyticsPercentageItemFilter value;

  private AnalyticsPercentageItemFilter threshold;

  private String                        lang             = null;

  private long                          offset           = 0;

  private long                          limit            = 0;

  public AnalyticsAggregation computeXAxisAggregation() {
    if (fromDate == null || toDate == null) {
      return null;
    }
    AnalyticsAggregation xAxisAggregation = new AnalyticsAggregation();
    xAxisAggregation.setField("timestamp");
    xAxisAggregation.setSortDirection("DESC");
    xAxisAggregation.setType(AnalyticsAggregationType.DATE);
    long daysPeriod = ChronoUnit.DAYS.between(fromDate, toDate);
    xAxisAggregation.setInterval(daysPeriod + "d");
    return xAxisAggregation;
  }

  public AnalyticsAggregation computeValueYAggregations() {
    return value == null ? null : value.getYAxisAggregation();
  }

  public AnalyticsAggregation computeThresholdYAggregations() {
    return threshold == null ? null : threshold.getYAxisAggregation();
  }

  public List<AnalyticsFieldFilter> computeValueFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    if (fromDate != null && toDate != null) {
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

    if (fromDate != null && toDate != null) {
      filters.add(computePeriodFilter());
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
    if (fromDate != null && toDate != null) {
      aggregations.add(computeXAxisAggregation());
    }
    if (value != null && value.getYAxisAggregation() != null) {
      aggregations.add(value.getYAxisAggregation());
    }
    return aggregations;
  }

  public List<AnalyticsAggregation> computeThresholdAggregations() {
    List<AnalyticsAggregation> aggregations = new ArrayList<>();
    if (fromDate != null && toDate != null) {
      aggregations.add(computeXAxisAggregation());
    }
    if (threshold != null && threshold.getYAxisAggregation() != null) {
      aggregations.add(threshold.getYAxisAggregation());
    }
    return aggregations;
  }

  public void setFromDateInMS(long timestampInMS) {
    fromDate = Instant.ofEpochMilli(timestampInMS).atZone(ZoneOffset.UTC).toLocalDate();
  }

  public void setToDateInMS(long timestampInMS) {
    toDate = Instant.ofEpochMilli(timestampInMS).atZone(ZoneOffset.UTC).toLocalDate();
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
    LocalDate clonedFromDate = fromDate == null ? null : LocalDate.from(fromDate);
    LocalDate clonedToDate = toDate == null ? null : LocalDate.from(toDate);
    AnalyticsFieldFilter clonedScopeFilter = scopeFilter == null ? null : scopeFilter.clone();
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterValue = value == null ? null : value.clone();
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterThreshold = threshold == null ? null
                                                                                                  : threshold.clone();

    return new AnalyticsPercentageFilter(title,
                                         chartType,
                                         colors,
                                         clonedScopeFilter,
                                         clonedFromDate,
                                         clonedToDate,
                                         cloneAnalyticsPercentageItemFilterValue,
                                         cloneAnalyticsPercentageItemFilterThreshold,
                                         lang,
                                         offset,
                                         limit);
  }

  private AnalyticsFieldFilter computePeriodFilter() {
    long daysPeriod = ChronoUnit.DAYS.between(fromDate, toDate);
    LocalDate fromDateFilter = fromDate.minusDays(daysPeriod);
    return new AnalyticsFieldFilter("timestamp",
                                    RANGE,
                                    new AnalyticsFilter.Range(fromDateFilter.atStartOfDay()
                                                                            .toInstant(ZoneOffset.UTC)
                                                                            .toEpochMilli(),
                                                              toDate.atStartOfDay()
                                                                    .toInstant(ZoneOffset.UTC)
                                                                    .toEpochMilli()));
  }

}
