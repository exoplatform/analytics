package org.exoplatform.analytics.api.processor;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class StatisticDataProcessorPlugin extends BaseComponentPlugin {

  public abstract String getId();

  public abstract void process(StatisticData data);

}
