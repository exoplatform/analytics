package org.exoplatform.analytics.api.service;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import lombok.Getter;
import lombok.Setter;

public class StatisticUIWatcherPlugin extends BaseComponentPlugin {

  private static final String PARAM_NAME = "watcher";

  @Getter
  @Setter
  private StatisticWatcher    statisticWatcher;

  public StatisticUIWatcherPlugin(InitParams params) {
    if (params == null || !params.containsKey(PARAM_NAME)) {
      throw new IllegalStateException("'watcher' init param is mandatory");
    }
    this.statisticWatcher = (StatisticWatcher) params.getObjectParam(PARAM_NAME).getObject();
  }
}
