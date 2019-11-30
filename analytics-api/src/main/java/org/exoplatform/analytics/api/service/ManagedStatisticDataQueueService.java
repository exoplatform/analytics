package org.exoplatform.analytics.api.service;

import org.picocontainer.Startable;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

@Managed
@NameTemplate(@Property(key = "service", value = "StatisticDataQueueService"))
@ManagedDescription("Satistics data queue service")
public class ManagedStatisticDataQueueService implements Startable {

  private StatisticDataQueueService statisticDataQueueService;

  public ManagedStatisticDataQueueService(StatisticDataQueueService statisticDataQueueService) {
    this.statisticDataQueueService = statisticDataQueueService;
  }

  @Override
  public void start() {
    // Added as startable to force instantiate it
  }

  @Override
  public void stop() {
    // Added as startable to force instantiate it
  }

  @Managed
  @ManagedDescription("Retrieve queue size")
  public long queueSize() {
    return statisticDataQueueService.queueSize();
  }

  @Managed
  @ManagedDescription("Return average queue processing execution time in ms")
  public long getAverageExecutionTime() {
    return statisticDataQueueService.getAverageExecutionTime();
  }

  @Managed
  @ManagedDescription("Return last queue processing execution time in ms")
  public long getLastExecutionTime() {
    return statisticDataQueueService.getLastExecutionTime();
  }

  @Managed
  @ManagedDescription("Return queue processing execution count")
  public long getExecutionCount() {
    return statisticDataQueueService.getExecutionCount();
  }
}
