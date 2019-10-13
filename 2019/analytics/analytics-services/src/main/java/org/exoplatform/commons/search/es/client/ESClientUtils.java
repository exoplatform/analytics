package org.exoplatform.commons.search.es.client;

import org.exoplatform.addon.analytics.es.AnalyticsIndexingServiceConnector;

/**
 * TODO add retrieving mapping method into {@link ElasticSearchingClient}
 */
public class ESClientUtils {

  private ESClientUtils() {
  }

  public static ElasticResponse getMappings(ElasticSearchingClient elasticSearchingClient) {
    String url = elasticSearchingClient.urlClient + "/" + AnalyticsIndexingServiceConnector.ES_ALIAS + "/_mapping";
    return elasticSearchingClient.sendHttpGetRequest(url);
  }
}
