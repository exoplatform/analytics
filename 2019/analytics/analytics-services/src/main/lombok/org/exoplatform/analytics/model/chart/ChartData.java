package org.exoplatform.analytics.model.chart;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
public class ChartData implements Serializable {

  private static final long            serialVersionUID   = 7951982952095482899L;

  private ChartAggregationValue        key;

  private List<ChartAggregationResult> aggregationResults = new ArrayList<>();

  private String                       lang;

  private String                       chartLabel;

  public String getChartKey() {
    return key == null || key.getAggregation() == null ? null
                                                       : key.getAggregation().getField();
  }

  public void addAggregationResult(ChartAggregationResult aggregationResult, boolean replaceIfExists) {
    ChartAggregationResult foundResult = aggregationResults.stream()
                                                           .filter(result -> result.getChartLabel()
                                                                                   .compareTo(aggregationResult.getChartLabel()) == 0)
                                                           .findFirst()
                                                           .orElse(null);
    if (foundResult == null) {
      aggregationResults.add(aggregationResult);
    } else if (replaceIfExists) {
      aggregationResults.remove(foundResult);
      aggregationResults.add(aggregationResult);
    }
  }

  public String getChartValue() {
    return key == null ? null : key.getFieldValue();
  }

  public List<String> getValues() {
    Collections.sort(aggregationResults);
    return aggregationResults.stream().map(ChartAggregationResult::getValue).collect(Collectors.toList());
  }

}
