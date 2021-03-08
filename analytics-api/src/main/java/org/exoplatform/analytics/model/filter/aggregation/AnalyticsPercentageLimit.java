package org.exoplatform.analytics.model.filter.aggregation;

import java.io.Serializable;

import org.exoplatform.analytics.model.filter.AnalyticsPercentageItemFilter;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPercentageLimit implements Serializable, Cloneable {

  private static final long             serialVersionUID = 3248761176436357754L;

  private AnalyticsPercentageItemFilter aggregation;

  private String                        field;

  private double                        percentage;

  @Override
  public AnalyticsPercentageLimit clone() {// NOSONAR
    AnalyticsPercentageItemFilter cloneyAggregation = aggregation == null ? null : aggregation.clone();
    return new AnalyticsPercentageLimit(cloneyAggregation, field, percentage);
  }

}
