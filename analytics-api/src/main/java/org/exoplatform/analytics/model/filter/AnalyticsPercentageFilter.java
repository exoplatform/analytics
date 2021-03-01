package org.exoplatform.analytics.model.filter;

import static org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType.RANGE;

import java.io.Serializable;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

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

  private String                        color;

  private AnalyticsFieldFilter          scopeFilter;

  private LocalDate                     fromDate;

  private LocalDate                     toDate;

  private AnalyticsPercentageItemFilter valueFilter;

  private AnalyticsPercentageItemFilter thresholdFilter;

  private String                        lang             = null;

  private long                          offset           = 0;

  private long                          limit            = 0;

  public List<AnalyticsFieldFilter> getValueFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    if (fromDate != null && toDate != null) {
      Period period = Period.between(fromDate, toDate);
      int daysPeriod = period.getDays();
      LocalDate fromDateFilter = fromDate.minusDays(daysPeriod);
      AnalyticsFieldFilter dateFilter = new AnalyticsFieldFilter("timestamp",
                                                                 RANGE,
                                                                 new AnalyticsFilter.Range(Instant.from(fromDateFilter)
                                                                                                  .toEpochMilli(),
                                                                                           Instant.from(toDate).toEpochMilli()));
      filters.add(dateFilter);
    }
    if (scopeFilter != null) {
      filters.add(scopeFilter);
    }
    if (valueFilter.getFilters() != null) {
      filters.addAll(valueFilter.getFilters());
    }
    return filters;
  }

  public List<AnalyticsFieldFilter> getThresholdFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    if (fromDate != null && toDate != null) {
      Period period = Period.between(fromDate, toDate);
      int daysPeriod = period.getDays();
      LocalDate fromDateFilter = fromDate.minusDays(daysPeriod);
      AnalyticsFieldFilter dateFilter = new AnalyticsFieldFilter("timestamp",
                                                                 RANGE,
                                                                 new AnalyticsFilter.Range(Instant.from(fromDateFilter)
                                                                                                  .toEpochMilli(),
                                                                                           Instant.from(toDate).toEpochMilli()));
      filters.add(dateFilter);
    }
    if (scopeFilter != null) {
      filters.add(scopeFilter);
    }
    if (valueFilter.getFilters() != null) {
      filters.addAll(thresholdFilter.getFilters());
    }
    return filters;
  }

  public List<AnalyticsAggregation> getValueAggregations() {
    List<AnalyticsAggregation> aggregations = new ArrayList<>();

    if (fromDate != null && toDate != null) {
      AnalyticsAggregation xAxisAggregation = new AnalyticsAggregation();
      xAxisAggregation.setField("timestamp");
      xAxisAggregation.setSortDirection("DESC");
      xAxisAggregation.setType(AnalyticsAggregationType.DATE);
      Period period = Period.between(fromDate, toDate);
      int daysPeriod = period.getDays();
      xAxisAggregation.setInterval(daysPeriod + "d");
      aggregations.add(xAxisAggregation);
    }
    if (valueFilter.getYAxisAggregation() != null) {
      aggregations.add(valueFilter.getYAxisAggregation());
    }
    return aggregations;
  }

  public List<AnalyticsAggregation> getThresholdAggregations() {
    List<AnalyticsAggregation> aggregations = new ArrayList<>();

    if (fromDate != null && toDate != null) {
      AnalyticsAggregation xAxisAggregation = new AnalyticsAggregation();
      xAxisAggregation.setField("timestamp");
      xAxisAggregation.setSortDirection("desc");
      xAxisAggregation.setType(AnalyticsAggregationType.DATE);
      Period period = Period.between(fromDate, toDate);
      int daysPeriod = period.getDays();
      xAxisAggregation.setInterval(daysPeriod + "d");
      aggregations.add(xAxisAggregation);
    }

    if (thresholdFilter != null && thresholdFilter.getYAxisAggregation() != null) {
      aggregations.add(thresholdFilter.getYAxisAggregation());
    }
    return aggregations;
  }

  public void setFromDateInMS(long timestampInMS) {
    fromDate = Instant.ofEpochMilli(timestampInMS).atZone(ZoneOffset.UTC).toLocalDate();
  }

  public void setToDateInMS(long timestampInMS) {
    toDate = Instant.ofEpochMilli(timestampInMS).atZone(ZoneOffset.UTC).toLocalDate();
  }

  @Override
  public AnalyticsPercentageFilter clone() { // NOSONAR
    LocalDate clonedFromDate = LocalDate.from(fromDate);
    LocalDate clonedToDate = LocalDate.from(toDate);
    AnalyticsFieldFilter clonedScopeFilter = scopeFilter == null ? scopeFilter.clone() : null;
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterValue = valueFilter.clone();
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterThreshold = thresholdFilter.clone();

    return new AnalyticsPercentageFilter(title,
                                         chartType,
                                         color,
                                         clonedScopeFilter,
                                         clonedFromDate,
                                         clonedToDate,
                                         cloneAnalyticsPercentageItemFilterValue,
                                         cloneAnalyticsPercentageItemFilterThreshold,
                                         lang,
                                         offset,
                                         limit);
  }

}
