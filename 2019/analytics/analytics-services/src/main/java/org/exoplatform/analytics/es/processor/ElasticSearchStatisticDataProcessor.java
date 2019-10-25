package org.exoplatform.analytics.es.processor;

import static org.exoplatform.analytics.utils.AnalyticsUtils.ES_ANALYTICS_PROCESSOR_ID;

import java.util.List;

import org.exoplatform.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.analytics.es.AnalyticsESClient;
import org.exoplatform.analytics.model.StatisticDataQueueEntry;

public class ElasticSearchStatisticDataProcessor extends StatisticDataProcessorPlugin {
  private AnalyticsESClient analyticsIndexingClient;

  public ElasticSearchStatisticDataProcessor(AnalyticsESClient analyticsIndexingClient) {
    this.analyticsIndexingClient = analyticsIndexingClient;
  }

  @Override
  public String getId() {
    return ES_ANALYTICS_PROCESSOR_ID;
  }

  @Override
  public void process(List<StatisticDataQueueEntry> processorQueueEntries) {
    analyticsIndexingClient.sendCreateBulkDocumentsRequest(processorQueueEntries);
  }

}
