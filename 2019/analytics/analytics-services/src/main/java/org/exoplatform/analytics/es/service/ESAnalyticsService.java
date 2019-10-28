package org.exoplatform.analytics.es.service;

import static org.exoplatform.analytics.es.ESAnalyticsUtils.ES_TYPE;
import static org.exoplatform.analytics.es.ESAnalyticsUtils.getIndex;
import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.*;
import org.picocontainer.Startable;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.es.AnalyticsESClient;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticData.StatisticStatus;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.chart.*;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.analytics.model.filter.AnalyticsFilter.Range;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.analytics.model.filter.search.*;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class ESAnalyticsService implements AnalyticsService, Startable {

  private static final Log                   LOG                            = ExoLogger.getLogger(ESAnalyticsService.class);

  private static final String                AGGREGATION_KEYS_SEPARATOR     = "-";

  private static final String                AGGREGATION_RESULT_PARAM       = "aggregation_result";

  private static final String                AGGREGATION_RESULT_VALUE_PARAM = "aggregation_result_value";

  private static final Context               CONTEXT                        = Context.GLOBAL.id("ANALYTICS");

  private static final Scope                 ES_SCOPE                       = Scope.GLOBAL.id("elasticsearch");

  private static final String                ES_AGGREGATED_MAPPING          = "ES_AGGREGATED_MAPPING";

  private static final AnalyticsFieldFilter  ES_TYPE_FILTER                 =
                                                            new AnalyticsFieldFilter("isAnalytics",
                                                                                     AnalyticsFieldFilterType.EQUAL,
                                                                                     "true");

  private AnalyticsESClient                  esClient;

  private SettingService                     settingService;

  private Map<String, StatisticFieldMapping> esMappings                     = new HashMap<>();

  private ScheduledExecutorService           esMappingUpdater               = Executors.newScheduledThreadPool(1);

  public ESAnalyticsService(SettingService settingService, AnalyticsESClient esClient) {
    this.esClient = esClient;
    this.settingService = settingService;
  }

  @Override
  public void start() {
    // Can't be job, because the mapping retrival must be executed on each
    // cluster node
    esMappingUpdater.scheduleAtFixedRate(() -> {
      try {
        retrieveMapping(true);
      } catch (Throwable e) {
        if (LOG.isDebugEnabled()) {
          LOG.warn("Error while checking connection status to Etherreum Websocket endpoint", e);
        } else {
          LOG.warn("Error while checking connection status to Etherreum Websocket endpoint: {}", e.getMessage());
        }
      }
    }, 0, 2, TimeUnit.MINUTES);
  }

  @Override
  public void stop() {
    esMappingUpdater.shutdown();
  }

  @Override
  public Set<StatisticFieldMapping> retrieveMapping(boolean forceRefresh) {
    if (!forceRefresh) {
      if (esMappings.isEmpty()) {
        readFieldsMapping();
      }
      return new HashSet<>(esMappings.values());
    }
    try {
      long timestamp = System.currentTimeMillis();
      String index = getIndex(timestamp);
      String mappingJsonString = esClient.getMapping(timestamp);
      if (StringUtils.isBlank(mappingJsonString)) {
        return new HashSet<>(esMappings.values());
      }
      JSONObject result = new JSONObject(mappingJsonString);
      JSONObject mappingObject = getJSONObject(result,
                                               0,
                                               index,
                                               "mappings",
                                               ES_TYPE,
                                               "properties");
      if (mappingObject != null) {
        String[] fieldNames = JSONObject.getNames(mappingObject);
        for (String fieldName : fieldNames) {
          JSONObject esField = mappingObject.getJSONObject(fieldName);
          String fieldType = esField.getString("type");
          JSONObject keywordField = getJSONObject(esField, 0, "fields", "keyword");
          StatisticFieldMapping esFieldMapping = new StatisticFieldMapping(fieldName, fieldType, keywordField != null);
          esMappings.put(fieldName, esFieldMapping);
        }
      }

      storeFieldsMappings();
    } catch (Exception e) {
      LOG.error("Error getting mapping of analytics", e);
    }
    return new HashSet<>(esMappings.values());
  }

  @Override
  public ChartDataList compueChartData(AnalyticsFilter filter) {
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

    String jsonResponse = this.esClient.sendRequest(esQueryString);
    try {
      return buildChartDataFromESResponse(filter, jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with \n - filter: " + filter + "\n - query: " + esQueryString
          + "\n - response: " + jsonResponse, e);
    }
  }

  @Override
  public List<StatisticData> retrieveData(AnalyticsFilter searchFilter) {
    StringBuilder esQuery = buildFilterQuery(searchFilter, false);

    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);
    LOG.debug("ES query to compute search count with filter {}: \n{}", searchFilter, esQueryString);

    String jsonResponse = this.esClient.sendRequest(esQueryString);
    try {
      return buildSearchResultFromESResponse(jsonResponse);
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

    if (!isCount && (sortFields == null || sortFields.isEmpty())) {
      sortFields = new ArrayList<>();
      sortFields.add(new AnalyticsSortField("timestamp", "desc"));
    }
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
    if (filters == null) {
      filters = new ArrayList<>();
    } else {
      filters = new ArrayList<>(filters);
    }
    filters.add(ES_TYPE_FILTER);

    esQuery.append(",\n");
    esQuery.append("    \"query\": {\n");
    esQuery.append("      \"bool\" : {\n");
    esQuery.append("        \"must\" : [\n");
    for (AnalyticsFieldFilter fieldFilter : filters) {
      String field = fieldFilter.getField();
      StatisticFieldMapping fieldMapping = this.esMappings.get(field);
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
               .append("\" : ")
               .append(fieldMapping.getESQueryValue(fieldFilter.getValueString()))
               .append("        }},\n");
        break;
      case GREATER:
        esQuery.append("        {\"range\" : {\"")
               .append(field)
               .append("\" : {")
               .append("\"gte\" : ")
               .append(fieldMapping.getESQueryValue(fieldFilter.getValueString()))
               .append("        }}},\n");
        break;
      case LESS:
        esQuery.append("        {\"range\" : {\"")
               .append(field)
               .append("\" : {")
               .append("\"lte\" : ")
               .append(fieldMapping.getESQueryValue(fieldFilter.getValueString()))
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
               .append(collectionToJSONString(fieldFilter.getValueString()))
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

        esQuery.append("       \"").append(fieldName).append("\": {");
        esQuery.append("         \"").append(aggregationType.getName()).append("\": {");
        esQuery.append("           \"field\": \"").append(analyticsAggregation.getField()).append("\"");
        if (aggregationType.isUseInterval()) {
          if (StringUtils.isBlank(analyticsAggregation.getInterval())) {
            throw new IllegalStateException("");
          }
          esQuery.append(",").append("           \"interval\": \"").append(analyticsAggregation.getInterval()).append("\"");
        } else if (aggregationType.allowSort()) {
          String sortDirection =
                               analyticsAggregation.getSortDirection() == null ? "asc" : analyticsAggregation.getSortDirection();
          esQuery.append(",").append("           \"order\": {\"_term\": \"").append(sortDirection).append("\"}");
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
    String lang = filter.getLang();
    if (buckets.length() > 0) {
      int nextLevel = level + 1;
      for (int i = 0; i < buckets.length(); i++) {
        JSONObject bucketResult = buckets.getJSONObject(i);
        ArrayList<ChartAggregationValue> childAggregationValues = new ArrayList<>();
        if (aggregationValues != null) {
          childAggregationValues.addAll(aggregationValues);
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
          addAggregationValue(key, filter, childAggregationValues, level);

          List<String> labels = childAggregationValues.stream().map(value -> value.getFieldLabel()).collect(Collectors.toList());
          String label = StringUtils.join(labels, AGGREGATION_KEYS_SEPARATOR);

          ChartAggregationLabel chartLabel = new ChartAggregationLabel(childAggregationValues, label, lang);
          ChartAggregationResult aggregationResult = new ChartAggregationResult(chartLabel, chartLabel.getLabel(), result);

          chartsData.addAggregationResult(parentAggregation, aggregationResult);
        } else {
          // An aggresgation in the middle of aggregations tree
          String key = bucketResult.getString("key");
          ChartAggregationValue parentAggregationToUse = parentAggregation;
          if (multipleChartsAggregation != null && level == -1) {
            String fieldLabel = multipleChartsAggregation.getLabel(key, filter.getLang());
            parentAggregationToUse = new ChartAggregationValue(multipleChartsAggregation, key, fieldLabel);
          } else {
            addAggregationValue(key, filter, childAggregationValues, level);
          }

          computeAggregatedResultEntry(filter,
                                       bucketResult,
                                       chartsData,
                                       multipleChartsAggregation,
                                       parentAggregationToUse,
                                       childAggregationValues,
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

    String fieldLabel = null;
    if (aggregation == null) {
      fieldLabel = key;
    } else {
      fieldLabel = aggregation.getLabel(key, filter.getLang());
    }

    ChartAggregationValue aggregationValue = new ChartAggregationValue(aggregation, key, fieldLabel);
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
      JSONObject statisticDataJsonObject = jsonHits.getJSONObject(i).getJSONObject("_source");
      int statusOrdinal = statisticDataJsonObject.getInt("status");
      statisticDataJsonObject.put("status", StatisticStatus.values()[statusOrdinal]);
      StatisticData statisticData = fromJsonString(statisticDataJsonObject.toString(), StatisticData.class);
      DEFAULT_FIELDS.stream().forEach(fieldName -> statisticDataJsonObject.remove(fieldName));

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

  private void readFieldsMapping() {
    SettingValue<?> existingMapping = settingService.get(CONTEXT, ES_SCOPE, ES_AGGREGATED_MAPPING);
    if (existingMapping == null) {
      return;
    }

    String esMappingSerialized = existingMapping.getValue().toString();
    try {
      JSONObject jsonObject = new JSONObject(esMappingSerialized);
      @SuppressWarnings("rawtypes")
      Iterator keys = jsonObject.keys();
      while (keys.hasNext()) {
        String key = keys.next().toString();
        String fieldMappingString = jsonObject.getString(key);
        StatisticFieldMapping fieldMapping = fromJsonString(fieldMappingString, StatisticFieldMapping.class);
        esMappings.put(key, fieldMapping);
      }
    } catch (JSONException e) {
      LOG.error("Error reading ES mapped fields");
    }
  }

  private void storeFieldsMappings() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    Set<String> keys = esMappings.keySet();
    for (String key : keys) {
      jsonObject.put(key, toJsonString(esMappings.get(key)));
    }
    settingService.set(CONTEXT,
                       ES_SCOPE,
                       ES_AGGREGATED_MAPPING,
                       SettingValue.create(jsonObject.toString()));
  }

}
