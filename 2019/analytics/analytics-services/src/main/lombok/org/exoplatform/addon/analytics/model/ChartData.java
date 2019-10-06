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

  private String            chartKey;

  private List<String>      data             = new ArrayList<>();

}
