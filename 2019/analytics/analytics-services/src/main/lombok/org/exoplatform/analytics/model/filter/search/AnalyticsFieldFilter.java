package org.exoplatform.analytics.model.filter.search;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ToString
@NoArgsConstructor
public class AnalyticsFieldFilter implements Serializable {

  private static final long        serialVersionUID = 5480543226777844698L;

  private String                   field;

  private AnalyticsFieldFilterType type;

  private String                   valueString;

  private Range                    range;

  public AnalyticsFieldFilter(String field, AnalyticsFieldFilterType type, String valueString) {
    this.field = field;
    this.type = type;
    this.valueString = valueString;
  }

  public AnalyticsFieldFilter(String field, AnalyticsFieldFilterType type, Range range) {
    this.field = field;
    this.type = type;
    this.range = range;
  }

  public AnalyticsFieldFilter(String field, AnalyticsFieldFilterType type) {
    this.field = field;
    this.type = type;
  }
}
