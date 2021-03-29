package org.exoplatform.analytics.model.filter.aggregation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AnalyticsAggregationType {

  SUM("sum", false, false, false),
  AVG("avg", false, false, false),
  MAX("max", false, false, false),
  MIN("min", false, false, false),
  TERMS("terms", false, true, true),
  COUNT("value_count", false, false, false),
  DATE("date_histogram", true, false, false),
  HISTOGRAM("histogram", true, false, false),
  CARDINALITY("cardinality", false, false, false);

  @Getter
  private String  aggName;

  @Getter
  private boolean useInterval;

  @Getter
  private boolean useSort;

  @Getter
  private boolean useLimit;

}
