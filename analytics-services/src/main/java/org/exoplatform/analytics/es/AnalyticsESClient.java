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

import static org.exoplatform.analytics.es.ESAnalyticsUtils.*;

import java.util.*;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONException;

import org.exoplatform.analytics.model.StatisticDataQueueEntry;
import org.exoplatform.commons.search.es.client.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsESClient extends ElasticClient {

  private static final Log                  LOG             = ExoLogger.getExoLogger(AnalyticsESClient.class);

  private AnalyticsIndexingServiceConnector analyticsIndexingConnector;

  private ElasticContentRequestBuilder      elasticContentRequestBuilder;

  private Set<String>                       existingIndexes = new HashSet<>();

  public AnalyticsESClient(ElasticIndexingAuditTrail auditTrail,
                           ElasticContentRequestBuilder elasticContentRequestBuilder,
                           AnalyticsIndexingServiceConnector analyticsIndexingConnector) {
    super(auditTrail);
    this.analyticsIndexingConnector = analyticsIndexingConnector;
    this.elasticContentRequestBuilder = elasticContentRequestBuilder;
    LOG.info("Using {} as Indexing URL for analytics", this.urlClient);
    this.urlClient = getESServerURL();
  }

  public boolean sendCreateIndexRequest(String index) {
    if (sendIsIndexExistsRequest(index)) {
      LOG.debug("Index {} already exists. Index creation requests will not be sent.", index);
      return false;
    } else {
      String indexURL = urlClient + "/" + index;
      String esIndexSettings = elasticContentRequestBuilder.getCreateIndexRequestContent(analyticsIndexingConnector);
      sendHttpPutRequest(indexURL, esIndexSettings);
      String esTypeURL = urlClient + "/" + index + "/_mapping/" + ES_TYPE;
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
      String index = getIndex(statisticDataQueueEntry.getStatisticData().getTimestamp());
      singleDocumentQuery = singleDocumentQuery.replace(ES_INDEX_PLACEHOLDER, index);
      request.append(singleDocumentQuery);
      indexesToUpdate.add(index);
    }

    LOG.debug("Create documents request to ES: \n {}", request);
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

  public String getMapping(long timestamp) {
    String esIndex = getIndex(timestamp);
    if (!sendIsIndexExistsRequest(esIndex)) {
      return null;
    }
    String url = urlClient + "/" + esIndex + "/_mapping";
    ElasticResponse response = sendHttpGetRequest(url);
    if (ElasticIndexingAuditTrail.isError(response.getStatusCode())) {
      LOG.warn("Error getting mapping of analytics : - \n\t\tcode : {} - \n\t\tmessage: {}",
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
    handleESResponse(response);
    return response;
  }

  @Override
  protected ElasticResponse sendHttpDeleteRequest(String url) {
    ElasticResponse response = super.sendHttpDeleteRequest(url);
    handleESResponse(response);
    return response;
  }

  @Override
  protected ElasticResponse sendHttpPostRequest(String url, String content) {
    ElasticResponse response = super.sendHttpPostRequest(url, content);
    handleESResponse(response);
    return response;
  }

  @Override
  protected String getEsUsernameProperty() {
    return getESServerUsername();
  }

  @Override
  protected String getEsPasswordProperty() {
    return getESServerPassword();
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
      indexes.add(getIndex(timestamp));
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
