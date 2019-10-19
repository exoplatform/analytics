package org.exoplatform.analytics.es.processor;

import static org.exoplatform.analytics.utils.AnalyticsUtils.ES_ANALYTICS_PROCESSOR_ID;

import org.exoplatform.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.analytics.es.connector.AnalyticsIndexingServiceConnector;
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
  public void process(StatisticData data, long dataId) {
    indexingService.index(AnalyticsIndexingServiceConnector.ES_TYPE, String.valueOf(dataId));
  }

}
