package org.exoplatform.analytics.model.filter;

import java.io.Serializable;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsTableColumnFilter implements Serializable, Cloneable {

  private static final long               serialVersionUID = 1650453023117737022L;

  private String                          title;

  private String                          userField;

  private String                          spaceField;

  private AnalyticsTableColumnAggregation valueAggregation;

  private AnalyticsTableColumnAggregation thresholdAggregation;

  private String                          dataType;

  private boolean                         sortable;

  private boolean                         previousPeriod;

  private String                          width;

  private String                          align;

  @Override
  public AnalyticsTableColumnFilter clone() { // NOSONAR
    AnalyticsTableColumnAggregation clonedAggregation = valueAggregation == null ? null : valueAggregation.clone();
    AnalyticsTableColumnAggregation clonedThresholdAggregation =
                                                               thresholdAggregation == null ? null : thresholdAggregation.clone();
    return new AnalyticsTableColumnFilter(title,
                                          userField,
                                          spaceField,
                                          clonedAggregation,
                                          clonedThresholdAggregation,
                                          dataType,
                                          sortable,
                                          previousPeriod,
                                          width,
                                          align);
  }
}
