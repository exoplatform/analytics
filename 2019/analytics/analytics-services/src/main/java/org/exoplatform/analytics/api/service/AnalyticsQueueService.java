package org.exoplatform.analytics.api.service;

import java.util.List;

import org.exoplatform.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.analytics.model.StatisticData;

public interface AnalyticsQueueService {

  void addProcessor(StatisticDataProcessorPlugin dataProcessorPlugin);

  void put(StatisticData data);

  StatisticData get(long timestamp);

  List<StatisticDataProcessorPlugin> getProcessors();

  int queueSize();
}
