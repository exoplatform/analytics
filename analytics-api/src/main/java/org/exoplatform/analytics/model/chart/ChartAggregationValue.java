package org.exoplatform.analytics.model.chart;

import java.io.Serializable;

import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
@AllArgsConstructor
public class ChartAggregationValue implements Serializable {

  private static final long    serialVersionUID = 3310521456018052347L;

  @Exclude
  private AnalyticsAggregation aggregation;

  private String               fieldValue;

  private String               fieldLabel;

}
