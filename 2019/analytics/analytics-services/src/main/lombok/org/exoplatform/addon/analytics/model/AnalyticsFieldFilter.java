package org.exoplatform.addon.analytics.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsFieldFilter {

  private String              field;

  private AnalyticsFieldFilterType type;

  private Object              value;
}
