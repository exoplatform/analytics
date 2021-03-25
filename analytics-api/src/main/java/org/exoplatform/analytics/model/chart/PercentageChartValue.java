package org.exoplatform.analytics.model.chart;

import java.io.Serializable;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PercentageChartValue implements Serializable {

  private static final long serialVersionUID = -7365271600124049313L;

  private double            currentPeriodValue;

  private double            previousPeriodValue;

  private long              computingTime;

  private long              dataCount;

}
