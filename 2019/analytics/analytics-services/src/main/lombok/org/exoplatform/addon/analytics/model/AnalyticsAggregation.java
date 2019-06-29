package org.exoplatform.addon.analytics.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class AnalyticsAggregation implements Serializable {

  private static final long        serialVersionUID = 2130321038232532587L;

  private AnalyticsAggregationType type;

  private String                   field;

}
