package org.exoplatform.analytics.model.chart;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
@AllArgsConstructor
public class ChartAggregationLabel implements Serializable {

  private static final long           serialVersionUID = 6189689375307105124L;

  @Exclude
  private List<ChartAggregationValue> aggregationValues;                      // NOSONAR

  private String                      label;

  @Exclude
  private String                      lang;

}
