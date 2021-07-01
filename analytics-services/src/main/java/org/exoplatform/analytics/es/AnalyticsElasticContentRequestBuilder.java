package org.exoplatform.analytics.es;

import org.exoplatform.commons.search.es.client.ElasticContentRequestBuilder;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;

public class AnalyticsElasticContentRequestBuilder extends ElasticContentRequestBuilder {

  @Override
  public String getCreateIndexRequestContent(ElasticIndexingServiceConnector connector) {
    return " {" +
        "\"aliases\": {" +
        "  \"" + connector.getIndexAlias() + "\": {" +
        "    \"is_write_index\" : true" +
        "  }" +
        "}" +
        "}";
  }

  public String getTurnOffWriteOnAllAnalyticsIndexes(AnalyticsIndexingServiceConnector connector) {
    return "{" +
        "\"actions\": [" +
        "  {" +
        "    \"add\": {" +
        "      \"index\": \"" + connector.getIndexPrefix() + "*\"," +
        "      \"alias\": \"" + connector.getIndexAlias() + "\"," +
        "      \"is_write_index\": false" +
        "    }" +
        "  }" +
        "]" +
        "}";
  }
}
