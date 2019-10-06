package org.exoplatform.addon.analytics.model.aggregation;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ToString
@NoArgsConstructor
public class AnalyticsAggregation implements Serializable {

  private static final long serialVersionUID = 2130321038232532587L;

  public AnalyticsAggregation(String field) {
    this.field = field;
    this.type = AnalyticsAggregationType.COUNT;
  }

  private AnalyticsAggregationType type;

  private String                   field;

  private String                   sortDirection;

}
