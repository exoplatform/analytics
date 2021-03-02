package org.exoplatform.analytics.model.chart;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PercentageChartDataList implements Serializable {

  private static final long serialVersionUID = 5490607865795348987L;

  private String            lang;

  private double            currentPeriodPercentage;

  private double            lastPeriodPercentage;

  private long              computingTime;

  private long              dataCount;

  public PercentageChartDataList(String lang) {
    this.lang = lang;
  }

}
