package org.exoplatform.addon.analytics.model.chart;

import java.io.Serializable;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChartData implements Serializable {

  private static final long serialVersionUID = 7951982952095482899L;

  private String            chartTitle;

}
