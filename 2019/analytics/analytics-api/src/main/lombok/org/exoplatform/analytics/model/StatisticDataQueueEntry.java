package org.exoplatform.analytics.model;

import java.io.Serializable;
import java.util.Map;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatisticDataQueueEntry implements Serializable {

  private static final long    serialVersionUID = -4173661081127997921L;

  private StatisticData        statisticData;

  private boolean              processed;

  private boolean              error;

  private short                attemptCount;

  private Map<String, Boolean> processingStatus;

  public StatisticDataQueueEntry(StatisticData statisticData) {
    this.statisticData = statisticData;
  }

  public long getId() {
    return statisticData == null ? 0 : Math.abs(statisticData.hashCode());
  }

}
