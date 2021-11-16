package org.exoplatform.analytics.model.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsTableColumnAggregation implements Serializable, Cloneable {

  private static final long          serialVersionUID = -7765464084735381851L;

  private AnalyticsAggregation       aggregation;

  private List<AnalyticsFieldFilter> filters          = new ArrayList<>();

  private boolean                    periodIndependent;

  private boolean                    countDateHistogramBuckets;

  @Override
  public AnalyticsTableColumnAggregation clone() { // NOSONAR
    AnalyticsAggregation clonedAggregation = aggregation == null ? null : aggregation.clone();
    List<AnalyticsFieldFilter> clonedFilters = filters.stream()
                                                      .map(AnalyticsFieldFilter::clone)
                                                      .collect(Collectors.toList());
    return new AnalyticsTableColumnAggregation(clonedAggregation, clonedFilters, periodIndependent, countDateHistogramBuckets);
  }
}
