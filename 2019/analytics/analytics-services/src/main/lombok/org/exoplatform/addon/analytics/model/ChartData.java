package org.exoplatform.addon.analytics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChartData implements Serializable {

  private static final long serialVersionUID = 7951982952095482899L;

  private String            chartTitle;

  private List<String>      labels           = new ArrayList<>();

  private List<String>      data             = new ArrayList<>();

  private long              searchDate;

  private long              computingTime;

  private long              dataCount;
}
