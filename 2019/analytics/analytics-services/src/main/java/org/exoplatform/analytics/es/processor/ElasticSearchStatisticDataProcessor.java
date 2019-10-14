package org.exoplatform.analytics.es.processor;

import static org.exoplatform.analytics.utils.AnalyticsUtils.ES_ANALYTICS_PROCESSOR_ID;

import org.exoplatform.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.analytics.es.AnalyticsIndexingServiceConnector;
import org.exoplatform.analytics.model.StatisticData;
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
