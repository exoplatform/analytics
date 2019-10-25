package org.exoplatform.analytics.api.service;

import java.util.*;
import java.util.stream.Collectors;

import org.exoplatform.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.analytics.model.StatisticDataQueueEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class StatisticDataProcessorService {

  private static final Log                        LOG                        =
                                                      ExoLogger.getLogger(StatisticDataProcessorService.class);

  private static final short                      MAX_PROCESS_ATTEMPTS_COUNT = 5;

  private static final short                      MAX_BULK_DOCUMENTS         = 20;

  private ArrayList<StatisticDataProcessorPlugin> dataProcessorPlugins       = new ArrayList<>();

  public void addProcessor(StatisticDataProcessorPlugin dataProcessorPlugin) {
    this.dataProcessorPlugins.add(dataProcessorPlugin);
  }

  public List<StatisticDataProcessorPlugin> getProcessors() {
    return this.dataProcessorPlugins;
  }

  public void process(List<StatisticDataQueueEntry> queueEntries) {
    if (queueEntries == null || queueEntries.isEmpty()) {
      return;
    }

    if (queueEntries.size() > MAX_BULK_DOCUMENTS) {
      // Process queue entries by chunk of 20 elements
      int entriesCount = queueEntries.size();
      int index = 0;
      do {
        int toIndex = Math.min(entriesCount, index + MAX_BULK_DOCUMENTS);
        List<StatisticDataQueueEntry> subList = queueEntries.subList(index, toIndex);
        process(subList);
        index = toIndex;
      } while (index < entriesCount);
      return;
    }

    Iterator<StatisticDataQueueEntry> queueEntriesIterator = queueEntries.iterator();
    while (queueEntriesIterator.hasNext()) {
      StatisticDataQueueEntry statisticDataQueueEntry = queueEntriesIterator.next();
      if (statisticDataQueueEntry.isProcessed()
          || (statisticDataQueueEntry.isError() && statisticDataQueueEntry.getAttemptCount() > MAX_PROCESS_ATTEMPTS_COUNT)) {
        queueEntriesIterator.remove();
      }
    }

    if (queueEntries.isEmpty()) {
      return;
    }

    boolean hasErrors = false;
    for (StatisticDataProcessorPlugin statisticDataProcessorPlugin : dataProcessorPlugins) {
      String processorId = statisticDataProcessorPlugin.getId();
      List<StatisticDataQueueEntry> processorQueueEntries = queueEntries.stream()
                                                                        .filter(queueEntry -> !queueEntry.isProcessorRun(processorId))
                                                                        .collect(Collectors.toList());
      try {
        statisticDataProcessorPlugin.process(processorQueueEntries);
        processorQueueEntries.forEach(queueEntry -> queueEntry.markProcessorAsSuccess(processorId));
      } catch (Exception e) {
        LOG.warn("Error processing queue entries, try to process entries one by one:\n{}", processorQueueEntries, e);

        // Try to process queue one by one to not block all queue entries
        processorQueueEntries.forEach(queueEntry -> {
          try {
            statisticDataProcessorPlugin.process(Collections.singletonList(queueEntry));
            queueEntry.markProcessorAsSuccess(processorId);
          } catch (Exception exception) {
            LOG.warn("Error processing queue entry {}", queueEntry, exception);
            queueEntry.markProcessorAsError(processorId);
          }
        });
        hasErrors = true;
      }
    }

    for (StatisticDataQueueEntry queueEntry : queueEntries) {
      queueEntry.setProcessed(!hasErrors);
      queueEntry.setError(hasErrors);
    }
  }

}
