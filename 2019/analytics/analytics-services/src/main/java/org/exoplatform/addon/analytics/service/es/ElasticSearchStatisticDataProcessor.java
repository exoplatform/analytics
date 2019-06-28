package org.exoplatform.addon.analytics.service.es;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.ES_ANALYTICS_PROCESSOR_ID;

import org.exoplatform.addon.analytics.api.StatisticDataProcessor;
import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.addon.analytics.service.AnalyticsService;
import org.exoplatform.services.listener.ListenerService;

public class ElasticSearchStatisticDataProcessor extends StatisticDataProcessor {
  private AnalyticsIndexingServiceConnector analyticsIndexingServiceConnector;

  public ElasticSearchStatisticDataProcessor(AnalyticsIndexingServiceConnector analyticsIndexingServiceConnector,
                                             AnalyticsService analyticsService,
                                             ListenerService listenerService) {
    super(analyticsService, listenerService);
  }

  @Override
  public String getId() {
    return ES_ANALYTICS_PROCESSOR_ID;
  }

  @Override
  public void persist(StatisticData data) {
    analyticsIndexingServiceConnector.create(String.valueOf(data.getDate().getTimestamp()));
  }

}
