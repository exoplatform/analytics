package org.exoplatform.addon.analytics.model.search;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSortField implements Serializable {

  private static final long serialVersionUID = 9197320072281514684L;

  private String            field;

  private String            direction        = "asc";
}
