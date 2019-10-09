package org.exoplatform.addon.analytics.model.chart;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@AllArgsConstructor
@EqualsAndHashCode
public class ChartAggregationResult implements Comparable<ChartAggregationResult>, Serializable {

  private static final long     serialVersionUID = 4036864369153698277L;

  private ChartAggregationLabel chartLabel;

  @Exclude
  @Getter
  private String                result;

  public String getLabel() {
    return chartLabel.getLabel();
  }

  public String getValue() {
    return StringUtils.isBlank(result) ? "0" : result;
  }

  @Override
  public int compareTo(ChartAggregationResult o) {
    if (o == null) {
      return 1;
    }
    if (chartLabel == o.chartLabel) {
      return 0;
    } else if (chartLabel == null) {
      return -1;
    } else if (o.chartLabel == null) {
      return 1;
    }
    return chartLabel.compareTo(o.chartLabel);
  }

  protected ChartAggregationLabel getChartLabel() {
    return chartLabel;
  }

}
