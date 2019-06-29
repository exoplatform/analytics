package org.exoplatform.addon.analytics.api.service;

import java.util.List;

import org.exoplatform.addon.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.addon.analytics.model.StatisticData;

public interface AnalyticsQueueService {

  void addProcessor(StatisticDataProcessorPlugin dataProcessorPlugin);

  void put(StatisticData data);

  StatisticData get(long timestamp);

  List<StatisticDataProcessorPlugin> getProcessors();

  int queueSize();
}
