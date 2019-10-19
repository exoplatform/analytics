package org.exoplatform.analytics.api.processor;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class StatisticDataProcessorPlugin extends BaseComponentPlugin {

  /**
   * @return processor identifier
   */
  public abstract String getId();

  /**
   * Process statistic data
   * 
   * @param data {@link StatisticData}
   * @param dataId unique identifier data from queue
   */
  public abstract void process(StatisticData data, long dataId);

}
