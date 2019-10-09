package org.exoplatform.addon.analytics.model.chart;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChartDataList implements Serializable {

  private static final long           serialVersionUID  = 5490607865795348987L;

  private String                      lang;

  private List<ChartAggregationLabel> aggregationLabels = new ArrayList<>();

  private List<ChartData>             charts            = new ArrayList<>();

  private long                        computingTime;

  private long                        dataCount;

  public ChartDataList(String lang) {
    this.lang = lang;
  }

  public void addChart(ChartData chartData) {
    charts.add(chartData);
  }

  public ChartData addResult(ChartAggregationValue chartParentAggregation, ChartAggregationResult aggregationResult) {
    ChartAggregationLabel chartLabel = aggregationResult.getChartLabel();

    if (!aggregationLabels.contains(chartLabel)) {
      aggregationLabels.add(chartLabel);
    }

    ChartData chartData = charts.stream()
                                .filter(data -> (data.getKey() == null && chartParentAggregation == null)
                                    || (data.getKey() != null && data.getKey().equals(chartParentAggregation)))
                                .findFirst()
                                .orElse(null);
    if (chartData == null) {
      List<ChartAggregationResult> results = new ArrayList<>();
      chartData = new ChartData(chartParentAggregation, results, lang);
      charts.add(chartData);
    }
    chartData.addAggregationResult(aggregationResult, true);
    return chartData;
  }

  public List<String> getLabels() {
    Collections.sort(aggregationLabels);
    return aggregationLabels.stream().map(result -> result.getLabel()).collect(Collectors.toList());
  }

  public void checkResults() {
    for (ChartAggregationLabel chartAggregationLabel : aggregationLabels) {
      ChartAggregationResult emptyResult = new ChartAggregationResult(chartAggregationLabel, null);
      charts.forEach(chartData -> chartData.addAggregationResult(emptyResult, false));
    }
  }
}
