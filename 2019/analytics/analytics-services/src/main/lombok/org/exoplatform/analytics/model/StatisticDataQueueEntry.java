package org.exoplatform.analytics.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatisticDataQueueEntry implements Serializable {

  private static final long       serialVersionUID = -4173661081127997921L;

  private static final AtomicLong ID_GENERATOR     = new AtomicLong();

  private StatisticData           statisticData;

  private boolean                 processed;

  private boolean                 error;

  private short                   attemptCount;

  private Map<String, Boolean>    processingStatus;

  private long                    id;

  public StatisticDataQueueEntry(StatisticData statisticData) {
    this.statisticData = statisticData;
  }

  public boolean isProcessorRun(String processorId) {
    Boolean processorStatus = processingStatus == null ? null : processingStatus.get(processorId);
    return processorStatus != null && processorStatus;
  }

  public void markProcessorAsSuccess(String processorId) {
    if (processingStatus == null) {
      processingStatus = new HashMap<>();
    }
    processingStatus.put(processorId, true);
  }

  public void markProcessorAsError(String processorId) {
    if (processingStatus == null) {
      processingStatus = new HashMap<>();
    }
    processingStatus.put(processorId, false);
    this.error = true;
  }

  public long getId() {
    if (id == 0) {
      id = ID_GENERATOR.incrementAndGet();
    }
    return id;
  }
}
