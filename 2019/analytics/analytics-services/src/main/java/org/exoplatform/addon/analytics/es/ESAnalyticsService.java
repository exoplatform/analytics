package org.exoplatform.addon.analytics.es;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.*;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.*;

import org.exoplatform.addon.analytics.api.service.AnalyticsQueueService;
import org.exoplatform.addon.analytics.api.service.AnalyticsService;
import org.exoplatform.addon.analytics.model.*;
import org.exoplatform.addon.analytics.model.aggregation.AnalyticsAggregation;
import org.exoplatform.addon.analytics.model.aggregation.AnalyticsAggregationType;
import org.exoplatform.addon.analytics.model.search.*;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class ESAnalyticsService extends AnalyticsService {
  private static final String    AGGREGATION_KEYS_SEPARATOR     = "-";

  private static final Log       LOG                            = ExoLogger.getLogger(ESAnalyticsService.class);

  private static final String    AGGREGATION_RESULT_PARAM       = "aggregation_result";

  private static final String    AGGREGATION_RESULT_VALUE_PARAM = "aggregation_result_value";

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
  public ChartDataList getChartData(AnalyticsFilter filter) {
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
      return buildChartDataFromESResponse(filter, jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with filter: " + filter + ", response: " + jsonResponse, e);
    }
  }

  @Override
  public List<StatisticData> getData(AnalyticsFilter searchFilter) {
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
  public int count(AnalyticsFilter searchFilter) {
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

  private void buildAnalyticsQuery(AnalyticsFilter analyticsFilter, boolean retrieveUsedTuples, StringBuilder esQuery) {
    List<AnalyticsSortField> sortFields = getSortFields(analyticsFilter.getXAxisAggregations());

    esQuery.append("{\n");
    buildSearchFilterQuery(esQuery,
                           analyticsFilter.getFilters(),
                           sortFields,
                           analyticsFilter.getOffset(),
                           analyticsFilter.getLimit(),
                           !retrieveUsedTuples);
    buildAggregationQuery(esQuery, analyticsFilter.getAggregations());
    esQuery.append("}");
  }

  private List<AnalyticsSortField> getSortFields(List<AnalyticsAggregation> aggregations) {
    List<AnalyticsSortField> sortFields = new ArrayList<>();
    aggregations.forEach(agg -> {
      AnalyticsSortField sortField = new AnalyticsSortField(agg.getField(), agg.getSortDirection());
      sortFields.add(sortField);
    });
    return sortFields;
  }

  private StringBuilder buildFilterQuery(AnalyticsFilter analyticsFilter, boolean isCount) {
    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");
    buildSearchFilterQuery(esQuery,
                           analyticsFilter.getFilters(),
                           null,
                           analyticsFilter.getOffset(),
                           analyticsFilter.getLimit(),
                           isCount);
    esQuery.append("}");
    return esQuery;
  }

  private void buildSearchFilterQuery(StringBuilder esQuery,
                                      List<AnalyticsFieldFilter> filters,
                                      List<AnalyticsSortField> sortFields,
                                      long offset,
                                      long limit,
                                      boolean isCount) {
    // If it's a count query, no need for results and no need for sort neither
    if (isCount) {
      esQuery.append("     \"size\" : 0");
    } else {
      if (offset > 0) {
        esQuery.append("     \"from\" : ").append(offset).append(",\n");
      }
      if (limit <= 0 || limit > Integer.MAX_VALUE) {
        limit = 10000;
      }
      esQuery.append("     \"size\" : ").append(limit).append(",\n");
      if (sortFields == null || sortFields.isEmpty()) {
        // Sort by date
        esQuery.append("     \"sort\" : [{ \"timestamp\":{\"order\" : \"asc\"}}]");
      } else {
        esQuery.append("     \"sort\" : [");
        for (int i = 0; i < sortFields.size(); i++) {
          AnalyticsSortField sortField = sortFields.get(i);
          if (sortField == null || sortField.getField() == null) {
            continue;
          }
          if (i > 0) {
            esQuery.append(",");
          }
          String direction = sortField.getDirection();
          if (direction == null) {
            direction = "asc";
          }
          esQuery.append("{ \"")
                 .append(sortField.getField())
                 .append("\":{\"order\" : \"")
                 .append(direction)
                 .append("\"}}");
        }
        esQuery.append("]");
      }
    }

    // Query body
    appendSearchFilterConditions(filters, esQuery);
    // End query body
  }

  private void appendSearchFilterConditions(List<AnalyticsFieldFilter> filters, StringBuilder esQuery) {
    if (filters != null && !filters.isEmpty()) {
      esQuery.append(",\n");
      esQuery.append("    \"query\": {\n");
      esQuery.append("      \"bool\" : {\n");
      esQuery.append("        \"must\" : [\n");
      for (AnalyticsFieldFilter fieldFilter : filters) {
        String field = fieldFilter.getField();
        if (!esKnownDataFields.contains(field.toLowerCase())) {
          field += ".keyword";
        }
        switch (fieldFilter.getType()) {
        case NOT_NULL:
          esQuery.append("        {\"exists\" : {\"")
                 .append("field")
                 .append("\" : \"")
                 .append(field)
                 .append("\"      }},\n");
          break;
        case IS_NULL:
          esQuery.append("        {\"bool\": {\"must_not\": {\"exists\": {\"field\": \"")
                 .append(field)
                 .append("\"      }}}},\n");
          break;
        case EQUAL:
          esQuery.append("        {\"match\" : {\"")
                 .append(field)
                 .append("\" : \"")
                 .append(fieldFilter.getValueString())
                 .append("\"")
                 .append("        }},\n");
          break;
        case GREATER:
          esQuery.append("        {\"range\" : {\"")
                 .append(field)
                 .append("\" : {")
                 .append("\"gte\" : ")
                 .append(fieldFilter.getValueString())
                 .append("        }}},\n");
          break;
        case LESS:
          esQuery.append("        {\"range\" : {\"")
                 .append(field)
                 .append("\" : {")
                 .append("\"lte\" : ")
                 .append(fieldFilter.getValueString())
                 .append("        }}},\n");
          break;
        case RANGE:
          Range range = fieldFilter.getRange();
          esQuery.append("        {\"range\" : {\"")
                 .append(field)
                 .append("\" : {")
                 .append("\"gte\" : ")
                 .append(range.getMin())
                 .append(",\"lte\" : ")
                 .append(range.getMax())
                 .append("        }}},\n");
          break;
        case IN_SET:
          esQuery.append("        {\"terms\" : {\"")
                 .append(field)
                 .append("\" : ")
                 .append(collectionToJSONString(fieldFilter.getValuesString()))
                 .append("        }},\n");
          break;
        default:
          break;
        }
      }
      esQuery.append("        ],\n");
      esQuery.append("      },\n");
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

  private ChartDataList buildChartDataFromESResponse(AnalyticsFilter filter, String jsonResponse) throws JSONException {
    ChartDataList charts = new ChartDataList();

    JSONObject json = new JSONObject(jsonResponse);

    JSONObject aggregations = json.getJSONObject("aggregations");
    if (aggregations == null) {
      return charts;
    }
    JSONObject hitsResult = (JSONObject) json.get("hits");

    charts.setComputingTime(json.getLong("took"));
    charts.setDataCount(hitsResult.getLong("total"));

    String parentKey = "";

    Map<String, String> resultInMap = new HashMap<>();
    computeAggregatedResultEntry(resultInMap, aggregations, parentKey);

    List<String> keys = new ArrayList<>(resultInMap.keySet());
    if (filter.isMultipleCharts()) {
      for (String key : keys) {
        String[] chartLabelKeys = key.split(AGGREGATION_KEYS_SEPARATOR);

        charts.addChartData(chartLabelKeys[0]);

        String xAxisLabel = StringUtils.join(chartLabelKeys, AGGREGATION_KEYS_SEPARATOR, 1, chartLabelKeys.length);
        if (!charts.getLabels().contains(xAxisLabel)) {
          charts.addKey(xAxisLabel);
        }
      }

      List<String> xAxisKeys = charts.getXAxisKeys();
      for (String xAxisKey : xAxisKeys) {
        for (ChartData chartData : charts.getCharts()) {
          String yAxisData = resultInMap.get(chartData.getChartKey() + AGGREGATION_KEYS_SEPARATOR + xAxisKey);
          if (yAxisData == null) {
            yAxisData = "0";
          }
          chartData.getData().add(yAxisData);
        }
      }
    } else {
      ChartData chartData = new ChartData();
      for (String key : keys) {
        charts.addKey(key);
        chartData.getData().add(resultInMap.get(key));
      }
      charts.addChart(chartData);
    }

    return charts;
  }

  private void computeAggregatedResultEntry(Map<String, String> resultInMap,
                                            JSONObject aggregations,
                                            String parentKey) throws JSONException {
    JSONObject aggsResult = aggregations.getJSONObject(AGGREGATION_RESULT_PARAM);
    JSONArray buckets = aggsResult.getJSONArray("buckets");
    if (buckets.length() > 0) {
      for (int i = 0; i < buckets.length(); i++) {
        JSONObject bucketResult = buckets.getJSONObject(i);
        if (bucketResult.isNull(AGGREGATION_RESULT_PARAM)) {
          String key = parentKey + bucketResult.getString("key");
          if (bucketResult.isNull(AGGREGATION_RESULT_VALUE_PARAM)) {
            resultInMap.put(key, bucketResult.get("doc_count").toString());
          } else {
            JSONObject valueResult = bucketResult.getJSONObject(AGGREGATION_RESULT_VALUE_PARAM);
            resultInMap.put(key, valueResult.get("value").toString());
          }
        } else {
          String key = parentKey + bucketResult.getString("key") + AGGREGATION_KEYS_SEPARATOR;
          computeAggregatedResultEntry(resultInMap, bucketResult, key);
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
