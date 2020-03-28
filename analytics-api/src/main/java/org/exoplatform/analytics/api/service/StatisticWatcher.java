package org.exoplatform.analytics.api.service;

import lombok.Data;

@Data
public class StatisticWatcher {

  private String name;

  private String selector;

  private String event;

  private int    maxItems;

}
