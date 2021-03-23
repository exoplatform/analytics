package org.exoplatform.analytics.model.chart;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@AllArgsConstructor
@EqualsAndHashCode
public class ChartAggregationResult implements Serializable {

  private static final long     serialVersionUID = 4036864369153698277L;

  @Exclude
  private ChartAggregationLabel chartLabel;

  private String                key;

  @Exclude
  @Getter
  private String                result;

  public String getLabel() {
    return chartLabel.getLabel();
  }

  public String getValue() {
    return StringUtils.isBlank(result) || StringUtils.equals(result, "null") ? "0" : result;
  }

  public ChartAggregationLabel retrieveChartLabel() {
    return chartLabel;
  }

}
