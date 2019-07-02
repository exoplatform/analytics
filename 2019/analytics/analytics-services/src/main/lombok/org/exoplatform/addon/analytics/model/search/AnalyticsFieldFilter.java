package org.exoplatform.addon.analytics.model.search;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsFieldFilter implements Serializable {

  private static final long        serialVersionUID = 5480543226777844698L;

  private String                   field;

  private AnalyticsFieldFilterType type;

  private Serializable             value;
}
