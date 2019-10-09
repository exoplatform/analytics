package org.exoplatform.addon.analytics.model.filter.aggregation;

import lombok.Getter;

public enum AnalyticsAggregationType {
  SUM("sum", false),
  AVG("avg", false),
  COUNT("terms", false),
  DATE("date_histogram", true),
  HISTOGRAM("histogram", true);

  @Getter
  private String  name;

  @Getter
  private boolean useInterval;

  private <T> AnalyticsAggregationType(String name, boolean useInterval) {
    this.name = name;
    this.useInterval = useInterval;
  }

  public boolean allowSort() {
    return this == COUNT;
  }
}
