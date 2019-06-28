package org.exoplatform.addon.analytics.model.chart;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LineChartData extends ChartData {

  private static final long serialVersionUID = -1378617894466946760L;

  private String            chartXLabels;

  private String            chartYData;

}
