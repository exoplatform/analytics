package org.exoplatform.analytics.api.service;

import org.exoplatform.analytics.model.StatisticData;

/**
 * A service to manage statistic data ingestion processing
 */
public interface StatisticDataQueueService {

  /**
   * Add {@link StatisticData} in statistics data ingestion queue
   * 
   * @param data
   */
  void put(StatisticData data);

  /**
   * Retrieve {@link StatisticData} from queue by its id
   * 
   * @param id unique identifier to retrieve {@link StatisticData} from queue
   * @return {@link StatisticData}
   */
  StatisticData get(long id);

  /**
   * @return statistic data ingestion queue size
   */
  int queueSize();
}
