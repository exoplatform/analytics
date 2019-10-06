package org.exoplatform.addon.analytics.model;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChartDataList implements Serializable {

  private static final long serialVersionUID = 5490607865795348987L;

  private List<ChartData>   charts           = new ArrayList<>();

  private List<String>      xAxisKeys        = new ArrayList<>();

  private long              computingTime;

  private long              dataCount;

  public void addChart(ChartData chartData) {
    charts.add(chartData);
  }

  public ChartData addChartData(String chartKey) {
    ChartData chartData = charts.stream()
                                .filter(data -> StringUtils.equalsIgnoreCase(data.getChartKey(), chartKey))
                                .findFirst()
                                .orElse(null);
    if (chartData == null) {
      chartData = new ChartData();
      chartData.setChartKey(chartKey);
      charts.add(chartData);
    }
    return chartData;
  }

  public Collection<String> getLabels() {
    return Collections.unmodifiableCollection(xAxisKeys);
  }

  public void addKey(String xAxisKey) {
    if (!xAxisKeys.contains(xAxisKey)) {
      xAxisKeys.add(xAxisKey);
    }
  }
}
