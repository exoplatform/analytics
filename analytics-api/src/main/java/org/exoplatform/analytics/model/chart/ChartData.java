package org.exoplatform.analytics.model.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

  public void addAggregationResult(ChartAggregationResult aggregationResult, int index, boolean replaceIfExists) {
    int existingIndex = aggregationResults.indexOf(aggregationResult);
    if (existingIndex < 0) {
      if (index < 0 || index >= aggregationResults.size()) {
        aggregationResults.add(aggregationResult);
      } else {
        aggregationResults.add(index, aggregationResult);
      }
    } else if (replaceIfExists) {
      aggregationResults.remove(existingIndex);
      if (index < 0 || index >= aggregationResults.size()) {
        aggregationResults.add(aggregationResult);
      } else {
        aggregationResults.add(index, aggregationResult);
      }
    }
  }

  public String getChartValue() {
    return key == null ? null : key.getFieldValue();
  }

  public List<String> getValues() {
    return aggregationResults.stream().map(ChartAggregationResult::getValue).collect(Collectors.toList());
  }

}
