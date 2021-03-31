package org.exoplatform.analytics.model.chart;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnItemValue {

  private String key;

  private Object value;

  private Object threshold;

  private Object previousValue;

  private Object previousThreshold;

}
