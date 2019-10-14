package org.exoplatform.analytics.model.chart;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
@AllArgsConstructor
public class ChartAggregationLabel implements Comparable<ChartAggregationLabel>, Serializable {

  private static final long           serialVersionUID           = 6189689375307105124L;

  private static final String         AGGREGATION_KEYS_SEPARATOR = "-";

  private List<ChartAggregationValue> aggregationValues;                                // NOSONAR

  @Exclude
  private String                      lang;

  public String getLabel() {
    List<String> labels = aggregationValues.stream().map(value -> value.getLabel(lang)).collect(Collectors.toList());
    return StringUtils.join(labels, AGGREGATION_KEYS_SEPARATOR);
  }

  @Override
  public int compareTo(ChartAggregationLabel o) {
    List<ChartAggregationValue> otherAggregationValues = o.getAggregationValues();
    if (aggregationValues == otherAggregationValues || (aggregationValues != null && otherAggregationValues != null
        && aggregationValues.isEmpty() && otherAggregationValues.isEmpty())) {
      return 0;
    } else if (aggregationValues == null) {
      return -1;
    } else if (otherAggregationValues == null) {
      return 1;
    }
    return compare(aggregationValues, otherAggregationValues, 0);
  }

  private int compare(List<ChartAggregationValue> aggregationValues,
                      List<ChartAggregationValue> otherAggregationValues,
                      int index) {
    if (index >= aggregationValues.size() || index >= otherAggregationValues.size()) {
      return aggregationValues.size() - otherAggregationValues.size();
    }
    ChartAggregationValue aggregationValue = aggregationValues.get(index);
    ChartAggregationValue otherAggregationValue = otherAggregationValues.get(index);
    int comparaison = aggregationValue.compareTo(otherAggregationValue);
    if (comparaison == 0) {
      return compare(aggregationValues, otherAggregationValues, index + 1);
    }
    return comparaison;
  }
}
