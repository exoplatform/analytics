package org.exoplatform.addon.analytics.model.chart;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
@AllArgsConstructor
public class ChartAggregationResult implements Comparable<ChartAggregationResult>, Serializable {

  private static final long     serialVersionUID = 4036864369153698277L;

  private ChartAggregationLabel chartLabel;

  @Exclude
  private String                aggregationResult;

  public String getValue() {
    return StringUtils.isBlank(aggregationResult) ? "0" : aggregationResult;
  }

  @Override
  public int compareTo(ChartAggregationResult o) {
    if (o == null) {
      return 1;
    }
    ChartAggregationLabel otherChartLabel = o.getChartLabel();
    if (chartLabel == otherChartLabel) {
      return 0;
    } else if (chartLabel == null) {
      return -1;
    } else if (otherChartLabel == null) {
      return 1;
    }
    return chartLabel.compareTo(otherChartLabel);
  }
}
