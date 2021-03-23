package org.exoplatform.analytics.model.filter;

import static org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType.RANGE;

import java.io.Serializable;
import java.time.*;
import java.util.*;

import org.exoplatform.analytics.model.filter.aggregation.*;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;

import groovy.transform.ToString;
import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPercentageFilter implements Serializable, Cloneable {

  private static final long             serialVersionUID    = 5699550622069979910L;

  private String                        title;

  private String                        chartType;

  private List<String>                  colors;

  private AnalyticsFieldFilter          scopeFilter;

  private String                        periodType;

  private AnalyticsPeriod               customPeriod;

  private LocalDate                     periodDate;

  private AnalyticsPercentageItemFilter value;

  private AnalyticsPercentageItemFilter threshold;

  private AnalyticsPercentageLimit      percentageLimit     = null;

  @Exclude
  private long                          currentPeriodLimit  = 0;

  @Exclude
  private long                          previousPeriodLimit = 0;

  private String                        lang                = null;

  public AnalyticsPeriodType getAnalyticsPeriodType() {
    return AnalyticsPeriodType.periodTypeByName(periodType);
  }

  public String getAnalyticsPeriodInterval() {
    AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
    if (analyticsPeriodType != null && periodDate != null) {
      return analyticsPeriodType.getCurrentPeriod(periodDate).getInterval();
    } else if (customPeriod != null) {
      return customPeriod.getInterval();
    }
    return null;
  }

  public AnalyticsPeriod getCurrentAnalyticsPeriod() {
    AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
    if (analyticsPeriodType != null && periodDate != null) {
      return analyticsPeriodType.getCurrentPeriod(periodDate);
    } else if (customPeriod != null) {
      return customPeriod.clone();
    }
    return null;
  }

  public AnalyticsPeriod getPreviousAnalyticsPeriod() {
    AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
    if (analyticsPeriodType != null && periodDate != null) {
      return analyticsPeriodType.getPreviousPeriod(periodDate);
    } else if (customPeriod != null) {
      return customPeriod.previousPeriod();
    }
    return null;
  }

  public void setPeriodDateInMS(long timestampInMS) {
    periodDate = Instant.ofEpochMilli(timestampInMS).atZone(ZoneOffset.UTC).toLocalDate();
  }

  public AnalyticsFilter computeValueFilter() {
    List<AnalyticsAggregation> xAxisAggregations = new ArrayList<>();
    AnalyticsAggregation xAxisAggregation = getXAxisAggregation();
    if (xAxisAggregation != null) {
      xAxisAggregations.add(xAxisAggregation);
    }
    long maxPeriodLimit = getMaxPeriodLimit();
    if (maxPeriodLimit > 0) {
      AnalyticsAggregation limitAggregation = new AnalyticsAggregation(percentageLimit.getField());
      limitAggregation.setType(AnalyticsAggregationType.TERMS);
      limitAggregation.setLimit(maxPeriodLimit);
      xAxisAggregations.add(limitAggregation);
    }

    return new AnalyticsFilter(title,
                               chartType,
                               colors,
                               getValueFilters(),
                               null,
                               xAxisAggregations,
                               getValueYAggregation(),
                               lang,
                               0l,
                               0l);
  }

  public AnalyticsFilter computeLimitFilter() {
    AnalyticsAggregation xAxisAggregation = getXAxisAggregation();
    return new AnalyticsFilter(title,
                               chartType,
                               colors,
                               getLimitFilters(),
                               null,
                               xAxisAggregation == null ? Collections.emptyList() : Collections.singletonList(xAxisAggregation),
                               getLimitYAggregation(),
                               lang,
                               0l,
                               0l);
  }

  public AnalyticsFilter computeThresholdFilter() {
    AnalyticsAggregation xAxisAggregation = getXAxisAggregation();
    return new AnalyticsFilter(title,
                               chartType,
                               colors,
                               getThresholdFilters(),
                               null,
                               xAxisAggregation == null ? Collections.emptyList() : Collections.singletonList(xAxisAggregation),
                               getThresholdYAggregation(),
                               lang,
                               0l,
                               0l);
  }

  public long getMaxPeriodLimit() {
    return Math.max(currentPeriodLimit, previousPeriodLimit);
  }

  @Override
  public AnalyticsPercentageFilter clone() { // NOSONAR
    LocalDate clonedPeriodDate = periodDate == null ? null : LocalDate.from(periodDate);
    AnalyticsPeriod clonedAnalyticsPeriod = customPeriod == null ? null : customPeriod.clone();
    AnalyticsFieldFilter clonedScopeFilter = scopeFilter == null ? null : scopeFilter.clone();
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterValue = value == null ? null : value.clone();
    AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterThreshold = threshold == null ? null
                                                                                                  : threshold.clone();
    AnalyticsPercentageLimit clonedAnalyticsPercentageLimit = percentageLimit == null ? null
                                                                                      : percentageLimit.clone();
    return new AnalyticsPercentageFilter(title,
                                         chartType,
                                         colors,
                                         clonedScopeFilter,
                                         periodType,
                                         clonedAnalyticsPeriod,
                                         clonedPeriodDate,
                                         cloneAnalyticsPercentageItemFilterValue,
                                         cloneAnalyticsPercentageItemFilterThreshold,
                                         clonedAnalyticsPercentageLimit,
                                         currentPeriodLimit,
                                         previousPeriodLimit,
                                         lang);
  }

  private AnalyticsAggregation getXAxisAggregation() {
    String interval = getAnalyticsPeriodInterval();
    if (interval == null) {
      return null;
    }
    AnalyticsAggregation xAxisAggregation = new AnalyticsAggregation();
    xAxisAggregation.setField("timestamp");
    xAxisAggregation.setSortDirection("DESC");
    xAxisAggregation.setType(AnalyticsAggregationType.DATE);
    xAxisAggregation.setInterval(interval);
    xAxisAggregation.setMinBound(getPreviousAnalyticsPeriod().getFromInMS());
    xAxisAggregation.setMaxBound(getCurrentAnalyticsPeriod().getToInMS() - 1000);
    if (customPeriod != null) {
      long offset = (xAxisAggregation.getMinBound() / 86400000l) % customPeriod.getDiffInDays();
      if (offset > 0) {
        xAxisAggregation.setOffset(offset + "d");
      }
    }
    return xAxisAggregation;
  }

  private AnalyticsAggregation getValueYAggregation() {
    return value == null ? null : value.getYAxisAggregation().clone();
  }

  private AnalyticsAggregation getLimitYAggregation() {
    return percentageLimit == null
        || percentageLimit.getAggregation() == null
        || percentageLimit.getAggregation().getYAxisAggregation() == null
                                                                          ? null
                                                                          : percentageLimit.getAggregation()
                                                                                           .getYAxisAggregation()
                                                                                           .clone();
  }

  private AnalyticsAggregation getThresholdYAggregation() {
    return threshold == null ? null : threshold.getYAxisAggregation().clone();
  }

  private List<AnalyticsFieldFilter> getValueFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    AnalyticsFieldFilter periodFilter = getPeriodFilter();
    if (periodFilter != null) {
      filters.add(periodFilter);
    }
    if (scopeFilter != null) {
      filters.add(scopeFilter);
    }
    if (value != null && value.getFilters() != null) {
      filters.addAll(value.getFilters());
    }
    return filters;
  }

  private List<AnalyticsFieldFilter> getLimitFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    AnalyticsFieldFilter periodFilter = getPeriodFilter();
    if (periodFilter != null) {
      filters.add(periodFilter);
    }
    if (scopeFilter != null) {
      filters.add(scopeFilter);
    }
    if (percentageLimit != null && percentageLimit.getAggregation() != null
        && percentageLimit.getAggregation().getFilters() != null) {
      filters.addAll(percentageLimit.getAggregation().getFilters());
    }
    return filters;
  }

  private List<AnalyticsFieldFilter> getThresholdFilters() {
    List<AnalyticsFieldFilter> filters = new ArrayList<>();

    AnalyticsFieldFilter periodFilter = getPeriodFilter();
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

  private AnalyticsFieldFilter getPeriodFilter() {
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
