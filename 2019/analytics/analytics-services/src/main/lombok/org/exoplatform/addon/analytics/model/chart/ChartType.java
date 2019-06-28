package org.exoplatform.addon.analytics.model.chart;

public enum ChartType {
  LINE(LineChartData.class),
  PIE(LineChartData.class);

  Class<? extends ChartData> chartDataClass = null;

  ChartType(Class<? extends ChartData> chartDataClass) {
    this.chartDataClass = chartDataClass;
  }

}
