package org.exoplatform.analytics.model.chart;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnItemValue {

  private String key;

  private String value;

  private String threshold;

  private String previousValue;

  private String previousThreshold;

}
