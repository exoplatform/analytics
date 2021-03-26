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
public class AnalyticsTableColumnFilter implements Serializable, Cloneable {

  private static final long          serialVersionUID = 1650453023117737022L;

  private String                     title;

  private String                     userField;

  private String                     spaceField;

  private AnalyticsAggregation       aggregation;

  private List<AnalyticsFieldFilter> filters          = new ArrayList<>();

  private String                     dataType;

  private boolean                    sortable;

  private boolean                    previousPeriod;

  @Override
  public AnalyticsTableColumnFilter clone() { // NOSONAR
    AnalyticsAggregation clonedAggregation = aggregation == null ? null : aggregation.clone();
    List<AnalyticsFieldFilter> clonedFilters = filters.stream()
                                                      .map(AnalyticsFieldFilter::clone)
                                                      .collect(Collectors.toList());
    return new AnalyticsTableColumnFilter(title,
                                          userField,
                                          spaceField,
                                          clonedAggregation,
                                          clonedFilters,
                                          dataType,
                                          sortable,
                                          previousPeriod);
  }
}
