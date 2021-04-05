package org.exoplatform.analytics.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticFieldValue {

  private String value;

  private long   count;

}
