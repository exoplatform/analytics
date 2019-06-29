package org.exoplatform.addon.analytics.es.processor;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.ES_ANALYTICS_PROCESSOR_ID;

import org.exoplatform.addon.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.addon.analytics.es.AnalyticsIndexingServiceConnector;
import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.commons.search.index.IndexingService;

public class ElasticSearchStatisticDataProcessor extends StatisticDataProcessorPlugin {
  private IndexingService indexingService;

  public ElasticSearchStatisticDataProcessor(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  @Override
  public String getId() {
    return ES_ANALYTICS_PROCESSOR_ID;
  }

  @Override
  public void process(StatisticData data) {
    indexingService.index(AnalyticsIndexingServiceConnector.ES_TYPE, String.valueOf(data.getTimestamp()));
  }

}
