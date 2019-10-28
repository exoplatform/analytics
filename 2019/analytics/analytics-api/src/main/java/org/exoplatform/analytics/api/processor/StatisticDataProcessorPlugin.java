package org.exoplatform.analytics.api.processor;

import java.util.List;

import org.exoplatform.analytics.model.StatisticDataQueueEntry;
import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class StatisticDataProcessorPlugin extends BaseComponentPlugin {

  /**
   * @return processor identifier
   */
  public abstract String getId();

  /**
   * Process statistic data
   * 
   * @param processorQueueEntries {@link List} of
   *          {@link StatisticDataQueueEntry}
   */
  public abstract void process(List<StatisticDataQueueEntry> processorQueueEntries);

}
