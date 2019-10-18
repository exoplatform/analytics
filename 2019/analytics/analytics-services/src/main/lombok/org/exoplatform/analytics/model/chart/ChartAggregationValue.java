package org.exoplatform.analytics.model.chart;

import java.io.Serializable;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartAggregationValue implements Comparable<ChartAggregationValue>, Serializable {

  private static final long    serialVersionUID = 3310521456018052347L;

  private AnalyticsAggregation aggregation;

  private String               fieldValue;

  private String               lang;

  private String               fieldLabel;

  public String getFieldLabel() {
    if (fieldLabel == null) {
      if (aggregation == null) {
        return fieldValue;
      }
      fieldLabel = aggregation.getLabel(fieldValue, lang);
    }
    return this.fieldLabel;
  }

  @Override
  public int compareTo(ChartAggregationValue o) {
    if (o == null) {
      return 1;
    }
    String otherFieldValue = o.getFieldValue();
    if (StringUtils.equals(fieldValue, otherFieldValue)) {
      return 0;
    } else if (fieldValue == null) {
      return -1;
    } else if (otherFieldValue == null) {
      return 1;
    }

    try {
      double fieldDouble = Double.parseDouble(fieldValue);
      double otherFieldDouble = Double.parseDouble(otherFieldValue);
      return fieldDouble > otherFieldDouble ? 1 : -1;
    } catch (NumberFormatException e) {
      // Nothing, expected in some cases
    }

    return fieldValue.compareTo(otherFieldValue);
  }

}
