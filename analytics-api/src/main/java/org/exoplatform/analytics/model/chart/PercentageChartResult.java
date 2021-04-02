package org.exoplatform.analytics.model.chart;

import java.io.Serializable;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PercentageChartResult implements Serializable {

  private static final long serialVersionUID = 5490607865795348987L;

  private double            currentPeriodValue;

  private double            currentPeriodThreshold;

  private double            previousPeriodValue;

  private double            previousPeriodThreshold;

  private double            currentPeriodLimit;

  private double            previousPeriodLimit;

  private long              computingTime;

  private long              dataCount;

}
