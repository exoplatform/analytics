package org.exoplatform.addon.analytics.es;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.*;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.*;

import org.exoplatform.addon.analytics.api.service.AnalyticsQueueService;
import org.exoplatform.addon.analytics.api.service.AnalyticsService;
import org.exoplatform.addon.analytics.model.*;
import org.exoplatform.addon.analytics.model.aggregation.AnalyticsAggregation;
import org.exoplatform.addon.analytics.model.aggregation.AnalyticsAggregationType;
import org.exoplatform.addon.analytics.model.search.AnalyticsFieldFilter;
import org.exoplatform.addon.analytics.model.search.AnalyticsSearchFilter;
import org.exoplatform.addon.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class ESAnalyticsService extends AnalyticsService {
  private static final String    AGGREGATION_RESULT_PARAM       = "aggregation_result";

  private static final String    AGGREGATION_RESULT_VALUE_PARAM = "aggregation_result_value";

  private static final Log       LOG                            = ExoLogger.getLogger(ESAnalyticsService.class);

  private List<String>           esKnownDataFields              = null;

  private ElasticSearchingClient esClient;

  public ESAnalyticsService(ElasticSearchingClient esClient, AnalyticsQueueService analyticsQueueService) {
    super(analyticsQueueService);
    this.esClient = esClient;

    esKnownDataFields = Arrays.stream(StatisticData.class.getDeclaredFields())
                              .map(field -> field.getName().toLowerCase())
                              .collect(Collectors.toList());
    esKnownDataFields.addAll(Arrays.stream(StatisticData.class.getDeclaredMethods())
                                   .filter(method -> method.getName().startsWith("get"))
                                   .map(method -> method.getName().replaceFirst("get", "").toLowerCase())
                                   .collect(Collectors.toList()));
  }

  @Override
  public ChartData getChartData(AnalyticsFilter filter) {
    boolean retrieveUsedTuples = false;

    StringBuilder esQuery = new StringBuilder();
    buildAnalyticsQuery(filter, retrieveUsedTuples, esQuery);

    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);
    LOG.info("ES query to compute chart data with filter {} \n{}", filter, esQueryString);

    String jsonResponse = this.esClient.sendRequest(esQueryString,
                                                    AnalyticsIndexingServiceConnector.ES_ALIAS,
                                                    AnalyticsIndexingServiceConnector.ES_TYPE);
    try {
      return buildChartDataFromESResponse(jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with filter: " + filter + ", response: " + jsonResponse, e);
    }
  }

  @Override
  public List<StatisticData> getData(AnalyticsSearchFilter searchFilter) {
    StringBuilder esQuery = buildFilterQuery(searchFilter, false);

    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);
    LOG.info("ES query to compute search count with filter {}: \n{}", searchFilter, esQueryString);

    String jsonResponse = this.esClient.sendRequest(esQueryString,
                                                    AnalyticsIndexingServiceConnector.ES_ALIAS,
                                                    AnalyticsIndexingServiceConnector.ES_TYPE);
    try {
      return buildSearchResultFromESResponse(jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with filter: " + searchFilter + ", response: " + jsonResponse, e);
    }
  }

  @Override
  public int count(AnalyticsSearchFilter searchFilter) {
    StringBuilder esQuery = buildFilterQuery(searchFilter, true);

    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);

    LOG.info("ES query to compute search count with filter {}: \n{}", searchFilter, esQueryString);
    String jsonResponse = this.esClient.sendRequest(esQueryString,
                                                    AnalyticsIndexingServiceConnector.ES_ALIAS,
                                                    AnalyticsIndexingServiceConnector.ES_TYPE);
    try {
      return getSearchCountFromESResponse(jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with filter: " + searchFilter + ", response: " + jsonResponse, e);
    }
  }

  private void buildAnalyticsQuery(AnalyticsFilter filter, boolean retrieveUsedTuples, StringBuilder esQuery) {
    esQuery.append("{\n");
    buildSearchFilterQuery(esQuery, filter.getSearchFilter(), !retrieveUsedTuples);
    buildAggregationQuery(esQuery, filter.getAggregations());
    esQuery.append("}");
  }

  private StringBuilder buildFilterQuery(AnalyticsSearchFilter searchFilter, boolean isCount) {
    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");
    buildSearchFilterQuery(esQuery, searchFilter, isCount);
    esQuery.append("}");
    return esQuery;
  }

  private void buildSearchFilterQuery(StringBuilder esQuery, AnalyticsSearchFilter searchFilter, boolean isCount) {
    // If it's a count query, no need for results and no need for sort neither
    if (isCount) {
      esQuery.append("     \"size\" : 0");
    } else {
      // Offset
      long offset = searchFilter == null ? 0 : searchFilter.getOffset();
      if (offset > 0) {
        esQuery.append("     \"from\" : ").append(offset).append(",\n");
      }
      // Limit
      long limit = searchFilter == null ? 10 : searchFilter.getLimit();
      if (limit >= 0 && limit < Long.MAX_VALUE) {
        limit = 10;
      }
      esQuery.append("     \"size\" : ").append(limit).append(",\n");
      // Sort by date
      esQuery.append("     \"sort\" : [{ \"timestamp\":{\"order\" : \"desc\"}}]");
    }

    // Query body
    appendSearchFilterConditions(searchFilter, esQuery);
    // End query body
  }

  private void appendSearchFilterConditions(AnalyticsSearchFilter searchFilter, StringBuilder esQuery) {
    if (searchFilter != null && !searchFilter.getFilters().isEmpty()) {
      esQuery.append(",\n");
      List<AnalyticsFieldFilter> filters = searchFilter.getFilters();
      if (filters != null && !filters.isEmpty()) {
        esQuery.append("    \"query\": {\n");
        esQuery.append("      \"bool\" : {\n");
        esQuery.append("        \"must\" : [\n");
        for (AnalyticsFieldFilter fieldFilter : filters) {
          String field = fieldFilter.getField();
          if (!esKnownDataFields.contains(field.toLowerCase())) {
            field += ".keyword";
          }
          switch (fieldFilter.getType()) {
          case EQUAL:
            esQuery.append("        {\"match\" : {\"")
                   .append(field)
                   .append("\" : \"")
                   .append(fieldFilter.getValue())
                   .append("\"")
                   .append("        }},\n");
            break;
          case GREATER:
            esQuery.append("        {\"range\" : {\"")
                   .append(field)
                   .append("\" : {")
                   .append("\"gte\" : ")
                   .append(fieldFilter.getValue())
                   .append("        }}},\n");
            break;
          case LESS:
            esQuery.append("        {\"range\" : {\"")
                   .append(field)
                   .append("\" : {")
                   .append("\"lte\" : ")
                   .append(fieldFilter.getValue())
                   .append("        }}},\n");
            break;
          case RANGE:
            @SuppressWarnings("unchecked")
            ImmutablePair<Long, Long> range = (ImmutablePair<Long, Long>) fieldFilter.getValue();
            esQuery.append("        {\"range\" : {\"")
                   .append(field)
                   .append("\" : {")
                   .append("\"gte\" : ")
                   .append(range.getKey())
                   .append(",\"lte\" : ")
                   .append(range.getValue())
                   .append("        }}},\n");
            break;
          case IN_SET:
            @SuppressWarnings("unchecked")
            Set<String> set = (Set<String>) fieldFilter.getValue();
            AnalyticsUtils.toJsonString(set);
            esQuery.append("        {\"terms\" : {\"")
                   .append(field)
                   .append("\" : ")
                   .append(collectionToJSONString(set))
                   .append("        }},\n");
            break;
          default:
            break;
          }
        }
        esQuery.append("        ],\n");
        esQuery.append("      },\n");
      }
      esQuery.append("     },\n");
    }
  }

  private void buildAggregationQuery(StringBuilder esQuery,
                                     List<AnalyticsAggregation> aggregations) {
    if (aggregations != null && !aggregations.isEmpty()) {
      StringBuffer endOfQuery = new StringBuffer();
      for (AnalyticsAggregation analyticsAggregation : aggregations) {
        esQuery.append("     ,\"aggs\": {");

        if (AnalyticsAggregationType.COUNT == analyticsAggregation.getType()) {
          esQuery.append("       \"").append(AGGREGATION_RESULT_PARAM).append("\": {");
        } else {
          esQuery.append("       \"").append(AGGREGATION_RESULT_VALUE_PARAM).append("\": {");
        }

        esQuery.append("         \"").append(analyticsAggregation.getType().getName()).append("\": {");
        esQuery.append("           \"field\": \"").append(analyticsAggregation.getField()).append("\"");
        esQuery.append("         }");

        // Appended at the end
        endOfQuery.append("       }");
        endOfQuery.append("     }");
      }
      esQuery.append(endOfQuery);
    }
  }

  private ChartData buildChartDataFromESResponse(String jsonResponse) throws JSONException {
    ChartData result = new ChartData();
    JSONObject json = new JSONObject(jsonResponse);

    JSONObject aggregations = json.getJSONObject("aggregations");
    if (aggregations == null) {
      return result;
    }
    JSONObject hitsResult = (JSONObject) json.get("hits");

    result.setComputingTime(json.getLong("took"));
    result.setDataCount(hitsResult.getLong("total"));

    String parentKey = "";

    computeAggregatedResultEntry(result, aggregations, parentKey);
    return result;
  }

  private void computeAggregatedResultEntry(ChartData chartData, JSONObject aggregations, String parentKey) throws JSONException {
    JSONObject aggsResult = aggregations.getJSONObject(AGGREGATION_RESULT_PARAM);
    JSONArray buckets = aggsResult.getJSONArray("buckets");
    if (buckets.length() > 0) {
      for (int i = 0; i < buckets.length(); i++) {
        JSONObject bucketResult = buckets.getJSONObject(i);
        String key = parentKey + bucketResult.getString("key") + "_";
        if (bucketResult.isNull(AGGREGATION_RESULT_PARAM)) {
          chartData.getChartXLabels().add(key);
          if (bucketResult.isNull(AGGREGATION_RESULT_VALUE_PARAM)) {
            chartData.getChartYData().add(bucketResult.get("doc_count").toString());
          } else {
            JSONObject valueResult = bucketResult.getJSONObject(AGGREGATION_RESULT_VALUE_PARAM);
            chartData.getChartYData().add(valueResult.get("value").toString());
          }
        } else {
          computeAggregatedResultEntry(chartData, bucketResult, key);
        }
      }
    }
  }

  private List<StatisticData> buildSearchResultFromESResponse(String jsonResponse) throws JSONException {
    List<StatisticData> results = new ArrayList<>();
    JSONObject json = new JSONObject(jsonResponse);

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) {
      return results;
    }

    JSONArray jsonHits = jsonResult.getJSONArray("hits");
    for (int i = 0; i < jsonHits.length(); i++) {
      JSONObject jsonObject = jsonHits.getJSONObject(i);
      JSONObject statisticDataJsonObject = jsonObject.getJSONObject("_source");
      StatisticData statisticData = fromJsonString(jsonObject.toString(), StatisticData.class);
      esKnownDataFields.stream().forEach(fieldName -> statisticDataJsonObject.remove(fieldName));

      statisticData.setParameters(new HashMap<>());
      Iterator<?> remainingKeys = statisticDataJsonObject.keys();
      while (remainingKeys.hasNext()) {
        String key = (String) remainingKeys.next();
        statisticData.getParameters().put(key, statisticDataJsonObject.getString(key));
      }

      results.add(statisticData);
    }
    return results;
  }

  private int getSearchCountFromESResponse(String jsonResponse) throws JSONException {
    JSONObject json = new JSONObject(jsonResponse);
    JSONObject jsonResult = json.getJSONObject("hits");
    if (jsonResult == null)
      return 0;
    return jsonResult.getInt("total");
  }

}
