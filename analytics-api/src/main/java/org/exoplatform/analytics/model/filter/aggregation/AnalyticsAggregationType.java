package org.exoplatform.analytics.model.filter.aggregation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AnalyticsAggregationType {

  SUM("sum", true, false, false, false),
  AVG("avg", true, false, false, false),
  MAX("max", true, false, false, false),
  MIN("min", true, false, false, false),
  COUNT("value_count", true, false, false, false),
  CARDINALITY("cardinality", true, false, false, false),
  TERMS("terms", false, false, true, true),
  DATE("date_histogram", false, true, false, false),
  HISTOGRAM("histogram", false, true, false, false);

  @Getter
  private String  aggName;

  @Getter
  private boolean numericResult;

  @Getter
  private boolean useInterval;

  @Getter
  private boolean useSort;

  @Getter
  private boolean useLimit;
}
