package org.exoplatform.addon.analytics.model.aggregation;

public enum AnalyticsAggregationType {
  SUM("sum"),
  AVG("avg"),
  COUNT("terms");

  private String name;

  private AnalyticsAggregationType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
