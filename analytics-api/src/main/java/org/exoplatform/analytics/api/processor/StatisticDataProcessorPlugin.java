package org.exoplatform.analytics.api.processor;

import java.util.List;

import org.exoplatform.analytics.model.StatisticDataQueueEntry;
import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class StatisticDataProcessorPlugin extends BaseComponentPlugin {

  protected boolean initialized;

  protected boolean paused;

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

  /**
   * initializes the processor
   */
  public void init() {
    this.initialized = true;
  }

  public boolean isInitialized() {
    return this.initialized;
  }

  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  public boolean isPaused() {
    return this.paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

}
