package org.exoplatform.analytics.api.service;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.analytics.model.StatisticDataQueueEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class StatisticDataProcessorService {

  private static final Log                        LOG                        =
                                                      ExoLogger.getLogger(StatisticDataProcessorService.class);

  private static final short                      MAX_PROCESS_ATTEMPTS_COUNT = 5;

  private ArrayList<StatisticDataProcessorPlugin> dataProcessorPlugins       = new ArrayList<>();

  public void addProcessor(StatisticDataProcessorPlugin dataProcessorPlugin) {
    this.dataProcessorPlugins.add(dataProcessorPlugin);
  }

  public List<StatisticDataProcessorPlugin> getProcessors() {
    return this.dataProcessorPlugins;
  }

  public void process(StatisticDataQueueEntry statisticDataQueueEntry) {
    if (statisticDataQueueEntry.isProcessed()
        || (statisticDataQueueEntry.isError() && statisticDataQueueEntry.getAttemptCount() > MAX_PROCESS_ATTEMPTS_COUNT)) {
      return;
    }

    boolean hasErrors = false;
    for (StatisticDataProcessorPlugin statisticDataProcessorPlugin : dataProcessorPlugins) {
      String processorId = statisticDataProcessorPlugin.getId();
      if (statisticDataQueueEntry.isProcessorRun(processorId)) {
        continue;
      }

      try {
        statisticDataProcessorPlugin.process(statisticDataQueueEntry.getStatisticData(), statisticDataQueueEntry.getId());
        statisticDataQueueEntry.markProcessorAsSuccess(processorId);
      } catch (Exception e) {
        LOG.warn("Error processing queue entry {}", statisticDataQueueEntry.getStatisticData(), e);
        statisticDataQueueEntry.markProcessorAsError(processorId);
        hasErrors = true;
      }
    }
    statisticDataQueueEntry.setProcessed(!hasErrors);
    statisticDataQueueEntry.setError(hasErrors);
  }

}
