/* 
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/ .
 */
package org.exoplatform.analytics.es;

import java.util.*;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONException;

import org.exoplatform.analytics.model.StatisticDataQueueEntry;
import org.exoplatform.commons.search.es.client.*;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsESClient extends ElasticClient {

  private static final Log                  LOG                            =
                                                ExoLogger.getExoLogger(AnalyticsESClient.class);

  private static final String               DEFAULT_ES_CLIENT_SERVER_URL   = "http://127.0.0.1:9200";

  private static final String               ES_CLIENT_SERVER_URL           = "exo.es.index.server.url";

  private static final String               ES_CLIENT_USERNAME             = "exo.es.index.server.username";

  private static final String               ES_CLIENT_PWD                  = "exo.es.index.server.password";           // NOSONAR

  private static final String               ES_ANALYTICS_CLIENT_SERVER_URL = "exo.es.analytics.index.server.url";

  private static final String               ES_ANALYTICS_CLIENT_USERNAME   = "exo.es.analytics.index.server.username";

  private static final String               ES_ANALYTICS_CLIENT_PWD        = "exo.es.analytics.index.server.password"; // NOSONAR

  private AnalyticsIndexingServiceConnector analyticsIndexingConnector;

  private ElasticContentRequestBuilder      elasticContentRequestBuilder;

  private Set<String>                       existingIndexes                = new HashSet<>();

  protected String                          username;

  private String                            password;

  public AnalyticsESClient(ElasticIndexingAuditTrail auditTrail,
                           ElasticContentRequestBuilder elasticContentRequestBuilder,
                           AnalyticsIndexingServiceConnector analyticsIndexingConnector,
                           InitParams initParams) {
    super(auditTrail);
    this.analyticsIndexingConnector = analyticsIndexingConnector;
    this.elasticContentRequestBuilder = elasticContentRequestBuilder;

    if (initParams != null) {
      if (initParams.containsKey(ES_ANALYTICS_CLIENT_SERVER_URL)) {
        this.urlClient = initParams.getValueParam(ES_ANALYTICS_CLIENT_SERVER_URL).getValue();
      }
      if (initParams.containsKey(ES_ANALYTICS_CLIENT_USERNAME)) {
        this.username = initParams.getValueParam(ES_ANALYTICS_CLIENT_USERNAME).getValue();
      }
      if (initParams.containsKey(ES_ANALYTICS_CLIENT_PWD)) {
        this.password = initParams.getValueParam(ES_ANALYTICS_CLIENT_PWD).getValue();
      }
    }

    if (StringUtils.isBlank(this.urlClient)) {
      this.urlClient = System.getProperty(ES_CLIENT_SERVER_URL);
      this.username = System.getProperty(ES_CLIENT_USERNAME);
      this.password = System.getProperty(ES_CLIENT_PWD);
    }

    if (StringUtils.isBlank(this.urlClient)) {
      this.urlClient = DEFAULT_ES_CLIENT_SERVER_URL;
    }
  }

  public boolean sendCreateIndexRequest(String index) {
    if (sendIsIndexExistsRequest(index)) {
      LOG.debug("Index {} already exists. Index creation requests will not be sent.", index);
      return false;
    } else {
      String indexURL = urlClient + "/" + index;
      String esIndexSettings = elasticContentRequestBuilder.getCreateIndexRequestContent(analyticsIndexingConnector);
      sendHttpPutRequest(indexURL, esIndexSettings);
      String esTypeURL = urlClient + "/" + index + "/_mapping/" + analyticsIndexingConnector.getType();
      sendHttpPutRequest(esTypeURL, analyticsIndexingConnector.getMapping());

      if (sendIsIndexExistsRequest(index)) {
        LOG.info("Index {} created.", index);
      } else {
        LOG.warn("Index {} seems not created successfully on ES.", index);
      }
      return true;
    }
  }

  public boolean sendIsIndexExistsRequest(String esIndex) {
    if (existingIndexes.contains(esIndex)) {
      return true;
    }
    ElasticResponse responseExists = sendHttpGetRequest(urlClient + "/" + esIndex);
    boolean indexExists = responseExists.getStatusCode() == HttpStatus.SC_OK;
    if (indexExists) {
      existingIndexes.add(esIndex);
    }
    return indexExists;
  }

  public void sendDeleteIndexRequest(String index) {
    sendHttpDeleteRequest(urlClient + "/" + index);
  }

  public void sendCreateBulkDocumentsRequest(List<StatisticDataQueueEntry> dataQueueEntries) {
    if (dataQueueEntries == null || dataQueueEntries.isEmpty()) {
      return;
    }

    LOG.info("Indexing in bulk {} documents", dataQueueEntries.size());

    checkIndexExistence(dataQueueEntries);

    StringBuilder request = new StringBuilder();
    Set<String> indexesToUpdate = new HashSet<>();
    for (StatisticDataQueueEntry statisticDataQueueEntry : dataQueueEntries) {
      String documentId = String.valueOf(statisticDataQueueEntry.getId());
      String singleDocumentQuery = elasticContentRequestBuilder.getCreateDocumentRequestContent(analyticsIndexingConnector,
                                                                                                documentId);
      String index = analyticsIndexingConnector.getIndex(statisticDataQueueEntry.getStatisticData().getTimestamp());
      singleDocumentQuery = analyticsIndexingConnector.replaceByIndexName(singleDocumentQuery, index);
      request.append(singleDocumentQuery);
      indexesToUpdate.add(index);
    }

    LOG.debug("Create documents request to ES: {}", request);
    String indexDocumentURL = urlClient + "/_bulk";
    sendHttpPutRequest(indexDocumentURL, request.toString());

    for (String index : indexesToUpdate) {
      refreshIndex(index);
    }
  }

  public String sendRequest(String esQuery) {
    String url = urlClient + "/_search";
    ElasticResponse elasticResponse = sendHttpPostRequest(url, esQuery);
    String response = elasticResponse.getMessage();
    int statusCode = elasticResponse.getStatusCode();
    if (ElasticIndexingAuditTrail.isError(statusCode) || StringUtils.isBlank(response)) {
      if (StringUtils.isBlank(response)) {
        response = "Empty response was sent by ES";
      }
    } else {
      org.json.JSONObject json = null;
      try {
        json = new org.json.JSONObject(response);
        if (json.has("status") && ElasticIndexingAuditTrail.isError(json.getInt("status"))) {
          throw new IllegalStateException("Error occured while requesting ES HTTP error code: '" + statusCode
              + "', HTTP response: '"
              + response + "'");
        }
      } catch (JSONException e) {
        throw new IllegalStateException("Error occured while requesting ES HTTP code: '" + statusCode
            + "', Error parsing response to JSON format, content = '" + response + "'", e);
      }
    }
    return response;
  }

  public String getMapping() {
    String url = urlClient + "/" + analyticsIndexingConnector.getIndexPrefix() + "*/_mapping";
    ElasticResponse response = sendHttpGetRequest(url);
    if (ElasticIndexingAuditTrail.isError(response.getStatusCode())) {
      LOG.warn("Error getting mapping of analytics : - \t\tcode : {} - \t\tmessage: {}",
               response.getStatusCode(),
               response.getMessage());
      return null;
    } else {
      return response.getMessage();
    }
  }

  @Override
  protected ElasticResponse sendHttpPutRequest(String url, String content) {
    ElasticResponse response = super.sendHttpPutRequest(url, content);
    try {
      handleESResponse(response);
    } catch (Exception e) {
      throw new ElasticClientException("Error sending PUT request '" + url + "' with content = '" + content + "'", e);
    }
    return response;
  }

  @Override
  protected ElasticResponse sendHttpDeleteRequest(String url) {
    ElasticResponse response = super.sendHttpDeleteRequest(url);
    try {
      handleESResponse(response);
    } catch (Exception e) {
      throw new ElasticClientException("Error sending 'DELETE' request '" + url + "'", e);
    }
    return response;
  }

  @Override
  protected ElasticResponse sendHttpPostRequest(String url, String content) {
    ElasticResponse response = super.sendHttpPostRequest(url, content);
    try {
      handleESResponse(response);
    } catch (Exception e) {
      throw new ElasticClientException("Error sending POST request '" + url + "' with content = '" + content + "'", e);
    }
    return response;
  }

  @Override
  protected String getEsUsernameProperty() {
    return username;
  }

  @Override
  protected String getEsPasswordProperty() {
    return password;
  }

  @Override
  protected HttpClientConnectionManager getClientConnectionManager() {
    return new PoolingHttpClientConnectionManager();
  }

  private void handleESResponse(ElasticResponse response) {
    if (response.getStatusCode() != 200) {
      throw new ElasticClientException(response.getMessage());
    }
    if (StringUtils.contains(response.getMessage(), "\"type\":\"version_conflict_engine_exception\"")) {
      LOG.warn("ID conflict in some content", response.getMessage());
      return;
    }
    if (response.getStatusCode() != 200 || StringUtils.contains(response.getMessage(), "\"errors\":true")) {
      throw new ElasticClientException(response.getMessage());
    }
  }

  private void checkIndexExistence(List<StatisticDataQueueEntry> dataQueueEntries) {
    Set<String> indexes = new HashSet<>();
    for (StatisticDataQueueEntry statisticDataQueueEntry : dataQueueEntries) {
      long timestamp = statisticDataQueueEntry.getStatisticData().getTimestamp();
      indexes.add(analyticsIndexingConnector.getIndex(timestamp));
    }
    for (String index : indexes) {
      sendCreateIndexRequest(index);
    }
  }

  private void refreshIndex(String index) {
    String indexRefreshURL = urlClient + "/" + index + "/_refresh";
    sendHttpPostRequest(indexRefreshURL, null);
  }

}
