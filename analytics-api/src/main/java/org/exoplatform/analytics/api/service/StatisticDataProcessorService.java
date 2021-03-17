package org.exoplatform.analytics.api.service;

import static org.exoplatform.analytics.utils.AnalyticsUtils.MAX_BULK_DOCUMENTS;

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

  private ArrayList<StatisticDataProcessorPlugin> dataProcessorPlugins       = new ArrayList<>();

  public void addProcessor(StatisticDataProcessorPlugin dataProcessorPlugin) {
    this.dataProcessorPlugins.add(dataProcessorPlugin);
  }

  public List<StatisticDataProcessorPlugin> getProcessors() {
    return this.dataProcessorPlugins;
  }

  public void process(List<? extends StatisticDataQueueEntry> queueEntries) {
    if (queueEntries == null || queueEntries.isEmpty()) {
      return;
    }

    if (queueEntries.size() > MAX_BULK_DOCUMENTS) {
      LOG.debug("Processing queue having {} documents with chunk of {}", queueEntries.size(), MAX_BULK_DOCUMENTS);

      // Process queue entries by chunk of MAX_BULK_DOCUMENTS elements
      int index = 0;
      do {
        int toIndex = Math.min(queueEntries.size(), index + MAX_BULK_DOCUMENTS);
        List<? extends StatisticDataQueueEntry> subList = queueEntries.subList(index, toIndex);
        process(subList);
      } while (!queueEntries.isEmpty());
      return;
    }

    Iterator<? extends StatisticDataQueueEntry> queueEntriesIterator = queueEntries.iterator();
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

    for (StatisticDataProcessorPlugin statisticDataProcessorPlugin : dataProcessorPlugins) {
      String processorId = statisticDataProcessorPlugin.getId();
      List<StatisticDataQueueEntry> processorQueueEntries = queueEntries.stream()
                                                                        .filter(queueEntry -> !isProcessorRun(queueEntry,
                                                                                                              processorId))
                                                                        .collect(Collectors.toList());
      if (processorQueueEntries.isEmpty()) {
        continue;
      }
      try {
        statisticDataProcessorPlugin.process(processorQueueEntries);
        processorQueueEntries.forEach(queueEntry -> markProcessorAsSuccess(queueEntry, processorId, dataProcessorPlugins));
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.warn("Error processing queue entries: {}, try to process entries one by one.", processorQueueEntries, e);
        } else {
          LOG.warn("Error processing queue entries, try to process entries one by one.", e);
        }

        // Try to process queue one by one to not block all queue entries
        processorQueueEntries.forEach(queueEntry -> {
          try {
            statisticDataProcessorPlugin.process(Collections.singletonList(queueEntry));
            markProcessorAsSuccess(queueEntry, processorId, dataProcessorPlugins);
          } catch (Exception exception) {
            LOG.warn("Error processing queue entry {}", queueEntry, exception);
            markProcessorAsError(queueEntry, processorId);
          }
        });
      } finally {
        processorQueueEntries.forEach(queueEntry -> queueEntry.setAttemptCount((short) (queueEntry.getAttemptCount() + 1)));
      }
    }
  }

  private void markProcessorAsSuccess(StatisticDataQueueEntry dataQueueEntry,
                                      String processorId,
                                      List<StatisticDataProcessorPlugin> processors) {
    if (dataQueueEntry.getProcessingStatus() == null) {
      dataQueueEntry.setProcessingStatus(new HashMap<>());
    }
    dataQueueEntry.getProcessingStatus().put(processorId, true);
    boolean processedForAll = isProcessedForAll(dataQueueEntry, processors);
    dataQueueEntry.setProcessed(processedForAll);
  }

  private void markProcessorAsError(StatisticDataQueueEntry dataQueueEntry, String processorId) {
    if (dataQueueEntry.getProcessingStatus() == null) {
      dataQueueEntry.setProcessingStatus(new HashMap<>());
    }
    dataQueueEntry.getProcessingStatus().put(processorId, false);
    dataQueueEntry.setError(true);
  }

  private boolean isProcessedForAll(StatisticDataQueueEntry dataQueueEntry, List<StatisticDataProcessorPlugin> processors) {
    if (processors == null || processors.isEmpty()) {
      return true;
    }
    for (StatisticDataProcessorPlugin statisticDataProcessorPlugin : processors) {
      if (!isProcessorRun(dataQueueEntry, statisticDataProcessorPlugin.getId())) {
        return false;
      }
    }
    return true;
  }

  private boolean isProcessorRun(StatisticDataQueueEntry dataQueueEntry, String processorId) {
    Boolean processorStatus =
                            dataQueueEntry.getProcessingStatus() == null ? null
                                                                         : dataQueueEntry.getProcessingStatus().get(processorId);
    return processorStatus != null && processorStatus;
  }

}
