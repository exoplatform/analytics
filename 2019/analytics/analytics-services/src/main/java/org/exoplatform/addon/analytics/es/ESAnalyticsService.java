package org.exoplatform.addon.analytics.es;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.*;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.*;

import org.exoplatform.addon.analytics.api.service.AnalyticsQueueService;
import org.exoplatform.addon.analytics.api.service.AnalyticsService;
import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.addon.analytics.model.chart.*;
import org.exoplatform.addon.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.addon.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.addon.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.addon.analytics.model.filter.search.*;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class ESAnalyticsService extends AnalyticsService {

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
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    if (filter.getAggregations() == null || filter.getAggregations().isEmpty()) {
      throw new IllegalArgumentException("Filter aggregations is mandatory");
    }
    boolean retrieveUsedTuples = false;

    StringBuilder esQuery = new StringBuilder();
    buildAnalyticsQuery(filter, retrieveUsedTuples, esQuery);

    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);
    LOG.debug("ES query to compute chart data with filter {} \n{}", filter, esQueryString);

    String jsonResponse = this.esClient.sendRequest(esQueryString,
                                                    AnalyticsIndexingServiceConnector.ES_ALIAS,
                                                    AnalyticsIndexingServiceConnector.ES_TYPE);
    try {
      return buildChartDataFromESResponse(filter, jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with \n - filter: " + filter + "\n - query: " + esQueryString
          + "\n - response: " + jsonResponse, e);
    }
  }

  @Override
  public List<StatisticData> getData(AnalyticsFilter searchFilter) {
    StringBuilder esQuery = buildFilterQuery(searchFilter, false);

    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);
    LOG.debug("ES query to compute search count with filter {}: \n{}", searchFilter, esQueryString);

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

    LOG.debug("ES query to compute search count with filter {}: \n{}", searchFilter, esQueryString);
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
    esQuery.append("{\n");
    buildSearchFilterQuery(esQuery,
                           analyticsFilter.getFilters(),
                           null,
                           analyticsFilter.getOffset(),
                           analyticsFilter.getLimit(),
                           !retrieveUsedTuples,
                           false);
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
    List<AnalyticsSortField> sortFields = isCount
        || analyticsFilter == null ? null : getSortFields(analyticsFilter.getXAxisAggregations());
    List<AnalyticsFieldFilter> filters = analyticsFilter == null ? Collections.emptyList() : analyticsFilter.getFilters();
    long offset = analyticsFilter == null ? 0 : analyticsFilter.getOffset();
    long limit = analyticsFilter == null ? 0 : analyticsFilter.getLimit();

    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");
    buildSearchFilterQuery(esQuery,
                           filters,
                           sortFields,
                           offset,
                           limit,
                           isCount,
                           !isCount);
    esQuery.append("}");
    return esQuery;
  }

  private void buildSearchFilterQuery(StringBuilder esQuery,
                                      List<AnalyticsFieldFilter> filters,
                                      List<AnalyticsSortField> sortFields,
                                      long offset,
                                      long limit,
                                      boolean isCount,
                                      boolean useSort) {
    // If it's a count query, no need for results and no need for sort neither
    if (isCount) {
      esQuery.append("     \"size\" : 0");
      if (useSort) {
        esQuery.append(",");
      }
    } else {
      if (offset > 0) {
        esQuery.append("     \"from\" : ").append(offset).append(",\n");
      }
      if (limit <= 0 || limit > Integer.MAX_VALUE) {
        limit = 10000;
      }
      esQuery.append("     \"size\" : ").append(limit).append(",\n");
    }

    if (useSort) {
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
      Iterator<AnalyticsAggregation> aggregationsIterator = aggregations.iterator();
      while (aggregationsIterator.hasNext()) {
        AnalyticsAggregation analyticsAggregation = aggregationsIterator.next();
        esQuery.append("     ,\"aggs\": {");

        String fieldName = null;
        AnalyticsAggregationType aggregationType = analyticsAggregation.getType();
        if (AnalyticsAggregationType.COUNT == aggregationType || AnalyticsAggregationType.DATE == aggregationType
            || AnalyticsAggregationType.HISTOGRAM == aggregationType) {
          fieldName = AGGREGATION_RESULT_PARAM;
        } else {
          fieldName = AGGREGATION_RESULT_VALUE_PARAM;
        }

        String sortDirection = analyticsAggregation.getSortDirection() == null ? "asc" : analyticsAggregation.getSortDirection();

        esQuery.append("       \"").append(fieldName).append("\": {");
        esQuery.append("         \"").append(aggregationType.getName()).append("\": {");
        esQuery.append("           \"field\": \"").append(analyticsAggregation.getField()).append("\",");
        if (aggregationType.isUseInterval()) {
          if (StringUtils.isBlank(analyticsAggregation.getInterval())) {
            throw new IllegalStateException("");
          }
          esQuery.append("           \"interval\": \"").append(analyticsAggregation.getInterval()).append("\"");
        } else {
          esQuery.append("           \"order\": {\"_term\": \"").append(sortDirection).append("\"}");
        }
        esQuery.append("         }");

        // Appended at the end
        endOfQuery.append("       }");
        endOfQuery.append("     }");
      }
      esQuery.append(endOfQuery);
    }
  }

  private ChartDataList buildChartDataFromESResponse(AnalyticsFilter filter, String jsonResponse) throws JSONException {
    ChartDataList chartsData = new ChartDataList(filter.getLang());

    JSONObject json = new JSONObject(jsonResponse);

    JSONObject aggregations = json.getJSONObject("aggregations");
    if (aggregations == null) {
      return chartsData;
    }
    JSONObject hitsResult = (JSONObject) json.get("hits");

    chartsData.setComputingTime(json.getLong("took"));
    chartsData.setDataCount(hitsResult.getLong("total"));

    int level = filter.isMultipleCharts() ? -1 : 0;
    AnalyticsAggregation multipleChartsAggregation = filter.getMultipleChartsAggregation();
    computeAggregatedResultEntry(filter, aggregations, chartsData, multipleChartsAggregation, null, null, level);
    chartsData.checkResults();
    return chartsData;
  }

  private void computeAggregatedResultEntry(AnalyticsFilter filter,
                                            JSONObject aggregations,
                                            ChartDataList chartsData,
                                            AnalyticsAggregation multipleChartsAggregation,
                                            ChartAggregationValue parentAggregation,
                                            ArrayList<ChartAggregationValue> aggregationValues,
                                            int level) throws JSONException {
    JSONObject aggsResult = aggregations.getJSONObject(AGGREGATION_RESULT_PARAM);
    JSONArray buckets = aggsResult.getJSONArray("buckets");
    if (buckets.length() > 0) {
      int nextLevel = level + 1;
      for (int i = 0; i < buckets.length(); i++) {
        JSONObject bucketResult = buckets.getJSONObject(i);
        ArrayList<ChartAggregationValue> childrenAggregationValues = new ArrayList<>();
        if (aggregationValues != null) {
          childrenAggregationValues.addAll(aggregationValues);
        }
        if (bucketResult.isNull(AGGREGATION_RESULT_PARAM)) {
          // Final result is found: last element in term of depth of
          // aggregations
          String key = bucketResult.getString("key");
          String result = null;
          if (bucketResult.isNull(AGGREGATION_RESULT_VALUE_PARAM)) {
            result = bucketResult.get("doc_count").toString();
          } else {
            JSONObject valueResult = bucketResult.getJSONObject(AGGREGATION_RESULT_VALUE_PARAM);
            result = valueResult.get("value").toString();
          }
          addAggregationValue(key, filter, childrenAggregationValues, level);
          ChartAggregationLabel chartLabel = new ChartAggregationLabel(childrenAggregationValues);
          ChartAggregationResult aggregationResult = new ChartAggregationResult(chartLabel, result);
          chartsData.addResult(parentAggregation, aggregationResult);
        } else {
          // An aggresgation in the middle of aggregations tree
          String key = bucketResult.getString("key");
          ChartAggregationValue parentAggregationToUse = parentAggregation;
          if (multipleChartsAggregation != null && level == -1) {
            parentAggregationToUse = new ChartAggregationValue(multipleChartsAggregation, key);
          } else {
            addAggregationValue(key, filter, childrenAggregationValues, level);
          }

          computeAggregatedResultEntry(filter,
                                       bucketResult,
                                       chartsData,
                                       multipleChartsAggregation,
                                       parentAggregationToUse,
                                       childrenAggregationValues,
                                       nextLevel);
        }
      }
    }
  }

  private void addAggregationValue(String key,
                                   AnalyticsFilter filter,
                                   ArrayList<ChartAggregationValue> aggregationValues,
                                   int level) {
    AnalyticsAggregation aggregation = null;
    if (filter.getXAxisAggregations().size() < level) {
      if (filter.getYAxisAggregation() == null) {
        throw new IllegalStateException("Can't find relative aggregation to index " + level);
      }
      aggregation = filter.getYAxisAggregation();
    } else {
      aggregation = filter.getXAxisAggregations().get(level);
    }
    ChartAggregationValue aggregationValue = new ChartAggregationValue(aggregation, key);
    aggregationValues.add(aggregationValue);
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
