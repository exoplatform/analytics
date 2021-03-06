package org.exoplatform.analytics.es.service;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.*;
import org.picocontainer.Startable;

import org.exoplatform.analytics.api.service.*;
import org.exoplatform.analytics.es.AnalyticsESClient;
import org.exoplatform.analytics.es.AnalyticsIndexingServiceConnector;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticData.StatisticStatus;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.chart.*;
import org.exoplatform.analytics.model.filter.*;
import org.exoplatform.analytics.model.filter.AnalyticsFilter.Range;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class ESAnalyticsService implements AnalyticsService, Startable {

  private static final Log                   LOG                                        =
                                                 ExoLogger.getLogger(ESAnalyticsService.class);

  private static final String                ANALYTICS_ADMIN_PERMISSION_PARAM_NAME      = "exo.analytics.admin.permissions";

  private static final String                ANALYTICS_VIEW_ALL_PERMISSION_PARAM_NAME   = "exo.analytics.viewall.permissions";

  private static final String                ANALYTICS_VIEW_PERMISSION_PARAM_NAME       = "exo.analytics.view.permissions";

  private static final String                RETURNED_AGGREGATION_DOCS_COUNT_PARAM_NAME =
                                                                                        "exo.analytics.aggregation.terms.doc_size";

  private static final String                AGGREGATION_KEYS_SEPARATOR                 = "-";

  private static final String                AGGREGATION_RESULT_PARAM                   = "aggregation_result";

  private static final String                AGGREGATION_RESULT_VALUE_PARAM             = "aggregation_result_value";

  private static final Context               CONTEXT                                    = Context.GLOBAL.id("ANALYTICS");

  private static final Scope                 ES_SCOPE                                   = Scope.GLOBAL.id("elasticsearch");

  private static final String                ES_AGGREGATED_MAPPING                      = "ES_AGGREGATED_MAPPING";

  private static final AnalyticsFieldFilter  ES_TYPE_FILTER                             =
                                                            new AnalyticsFieldFilter("isAnalytics",
                                                                                     AnalyticsFieldFilterType.EQUAL,
                                                                                     "true");

  private List<StatisticUIWatcherPlugin>     uiWatcherPlugins                           = new ArrayList<>();

  private List<StatisticWatcher>             uiWatchers                                 = new ArrayList<>();

  private AnalyticsESClient                  esClient;

  private AnalyticsIndexingServiceConnector  analyticsIndexingServiceConnector;

  private SettingService                     settingService;

  private Map<String, StatisticFieldMapping> esMappings                                 = new HashMap<>();

  private ScheduledExecutorService           esMappingUpdater                           = Executors.newScheduledThreadPool(1);

  private List<String>                       administratorsPermissions;

  private List<String>                       viewAllPermissions;

  private List<String>                       viewPermissions;

  private int                                aggregationReturnedDocumentsSize           = 200;

  public ESAnalyticsService(AnalyticsESClient esClient,
                            AnalyticsIndexingServiceConnector analyticsIndexingServiceConnector,
                            SettingService settingService,
                            InitParams params) {
    this.esClient = esClient;
    this.analyticsIndexingServiceConnector = analyticsIndexingServiceConnector;
    this.settingService = settingService;

    if (params != null && params.containsKey(ANALYTICS_ADMIN_PERMISSION_PARAM_NAME)) {
      this.administratorsPermissions = params.getValuesParam(ANALYTICS_ADMIN_PERMISSION_PARAM_NAME).getValues();
    } else {
      this.administratorsPermissions = Collections.emptyList();
    }
    if (params != null && params.containsKey(ANALYTICS_VIEW_ALL_PERMISSION_PARAM_NAME)) {
      this.viewAllPermissions = params.getValuesParam(ANALYTICS_VIEW_ALL_PERMISSION_PARAM_NAME).getValues();
    } else {
      this.viewAllPermissions = Collections.emptyList();
    }
    if (params != null && params.containsKey(ANALYTICS_VIEW_PERMISSION_PARAM_NAME)) {
      this.viewPermissions = params.getValuesParam(ANALYTICS_VIEW_PERMISSION_PARAM_NAME).getValues();
    } else {
      this.viewPermissions = Collections.emptyList();
    }
    if (params != null && params.containsKey(RETURNED_AGGREGATION_DOCS_COUNT_PARAM_NAME)) {
      this.aggregationReturnedDocumentsSize = Integer.parseInt(params.getValueParam(RETURNED_AGGREGATION_DOCS_COUNT_PARAM_NAME)
                                                                     .getValue());
    }
  }

  @Override
  public void start() {
    // Can't be job, because the mapping retrival must be executed on each
    // cluster node
    esMappingUpdater.scheduleAtFixedRate(() -> {
      PortalContainer container = PortalContainer.getInstance();
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        retrieveMapping(true);
      } catch (Exception e) {
        LOG.warn("Error while getting mapping from elasticsearch", e);
      } finally {
        RequestLifeCycle.end();
      }
    }, 1, 2, TimeUnit.MINUTES);
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
      long today = System.currentTimeMillis();
      String index = analyticsIndexingServiceConnector.getIndex(today);
      String mappingJsonString = esClient.getMapping(today);
      if (StringUtils.isBlank(mappingJsonString)) {
        return new HashSet<>(esMappings.values());
      }
      JSONObject result = new JSONObject(mappingJsonString);
      JSONObject mappingObject = getJSONObject(result,
                                               0,
                                               index,
                                               "mappings",
                                               analyticsIndexingServiceConnector.getType(),
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
  public PercentageChartDataList computeChartData(AnalyticsPercentageFilter filter) {
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    AnalyticsAggregation dateAxisAggregation = filter.computeXAxisAggregation();

    ChartDataList valueChartData = computeChartData(filter.computeValueFilter());
    ChartData valueChart = valueChartData.getCharts() == null
        || valueChartData.getCharts().isEmpty() ? null : valueChartData.getCharts().iterator().next();
    List<ChartAggregationResult> valueAggregationResults = valueChart == null
        || valueChart.getAggregationResults() == null ? Collections.emptyList() : valueChart.getAggregationResults();

    ChartDataList thresholdChartData = computeChartData(filter.computeThresholdFilter());
    ChartData thresholdChart = thresholdChartData.getCharts() == null
        || thresholdChartData.getCharts().isEmpty() ? null : thresholdChartData.getCharts().iterator().next();
    List<ChartAggregationResult> thresholdAggregationResults = thresholdChart == null
        || thresholdChart.getAggregationResults() == null ? Collections.emptyList() : thresholdChart.getAggregationResults();

    AnalyticsPeriod currentAnalyticsPeriod = filter.getCurrentAnalyticsPeriod();
    AnalyticsPeriod previousAnalyticsPeriod = filter.getPreviousAnalyticsPeriod();

    ChartAggregationResult currentValueResult = getChartDateAggregationResult(dateAxisAggregation,
                                                                              valueAggregationResults,
                                                                              currentAnalyticsPeriod);
    ChartAggregationResult previousValueResult = getChartDateAggregationResult(dateAxisAggregation,
                                                                               valueAggregationResults,
                                                                               previousAnalyticsPeriod);

    ChartAggregationResult currentThresholdResult = getChartDateAggregationResult(dateAxisAggregation,
                                                                                  thresholdAggregationResults,
                                                                                  currentAnalyticsPeriod);
    ChartAggregationResult previousThresholdResult = getChartDateAggregationResult(dateAxisAggregation,
                                                                                   thresholdAggregationResults,
                                                                                   previousAnalyticsPeriod);

    double currentPeriodValue = currentValueResult == null ? 0 : Double.parseDouble(currentValueResult.getValue());
    double currentPeriodThreshold = currentThresholdResult == null ? 0 : Double.parseDouble(currentThresholdResult.getValue());
    double previousPeriodValue = previousValueResult == null ? 0 : Double.parseDouble(previousValueResult.getValue());
    double previousPeriodThreshold = previousThresholdResult == null ? 0
                                                                     : Double.parseDouble(previousThresholdResult.getValue());
    return new PercentageChartDataList(currentPeriodValue,
                                       currentPeriodThreshold,
                                       previousPeriodValue,
                                       previousPeriodThreshold,
                                       valueChartData.getComputingTime() + thresholdChartData.getComputingTime(),
                                       valueChartData.getDataCount() + thresholdChartData.getDataCount());
  }

  @Override
  public ChartDataList computeChartData(AnalyticsFilter filter) {
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    if (filter.getAggregations() == null || filter.getAggregations().isEmpty()) {
      throw new IllegalArgumentException("Filter aggregations is mandatory");
    }

    String esQueryString = buildAnalyticsQuery(filter.getAggregations(),
                                               filter.getFilters(),
                                               filter.getOffset(),
                                               filter.getLimit());

    String jsonResponse = this.esClient.sendRequest(esQueryString);
    try {
      return buildChartDataFromESResponse(filter, jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with - filter: " + filter + " - query: " + esQueryString
          + " - response: " + jsonResponse, e);
    }
  }

  @Override
  public List<StatisticData> retrieveData(AnalyticsFilter filter) {
    List<AnalyticsFieldFilter> filters = filter == null ? Collections.emptyList() : filter.getFilters();
    long offset = filter == null ? 0 : filter.getOffset();
    long limit = filter == null ? 10 : filter.getLimit();

    String esQueryString = buildAnalyticsQuery(null, filters, offset, limit);

    String jsonResponse = this.esClient.sendRequest(esQueryString);
    try {
      return buildSearchResultFromESResponse(jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with filter: " + filter + ", response: " + jsonResponse, e);
    }
  }

  @Override
  public List<String> getAdministratorsPermissions() {
    return administratorsPermissions;
  }

  @Override
  public List<String> getViewAllPermissions() {
    return viewAllPermissions;
  }

  @Override
  public List<String> getViewPermissions() {
    return viewPermissions;
  }

  @Override
  public List<StatisticWatcher> getUIWatchers() {
    return uiWatchers;
  }

  @Override
  public StatisticWatcher getUIWatcher(String name) {
    return getUIWatchers().stream().filter(watcher -> StringUtils.equals(name, watcher.getName())).findFirst().orElse(null);
  }

  @Override
  public void addUIWatcherPlugin(StatisticUIWatcherPlugin uiWatcherPlugin) {
    uiWatcherPlugins.add(uiWatcherPlugin);
    uiWatchers.add(uiWatcherPlugin.getStatisticWatcher());
  }

  private String buildAnalyticsQuery(List<AnalyticsAggregation> aggregations,
                                     List<AnalyticsFieldFilter> filters,
                                     long offset,
                                     long limit) {
    boolean isCount = aggregations != null;

    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{");
    buildSearchFilterQuery(esQuery,
                           filters,
                           offset,
                           limit,
                           isCount);
    if (isCount) {
      buildAggregationQuery(esQuery, aggregations);
    }
    esQuery.append("}");
    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);
    LOG.debug("ES query to compute chart data with aggregations :{}, filters :{} . Query: {}",
              aggregations,
              filters,
              esQueryString);
    return esQueryString;
  }

  private void buildSearchFilterQuery(StringBuilder esQuery,
                                      List<AnalyticsFieldFilter> filters,
                                      long offset,
                                      long limit,
                                      boolean isCount) {
    // If it's a count query, no need for results and no need for sort neither
    if (isCount) {
      esQuery.append("     \"size\" : 0");
    } else {
      if (offset > 0) {
        esQuery.append("     \"from\" : ").append(offset).append(",");
      }
      if (limit <= 0 || limit > Integer.MAX_VALUE) {
        limit = 10000;
      }
      esQuery.append("     \"size\" : ").append(limit).append(",");
      // Sort by date
      esQuery.append("     \"sort\" : [{ \"timestamp\":{\"order\" : \"desc\"}}]");
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

    esQuery.append(",");
    esQuery.append("    \"query\": {");
    esQuery.append("      \"bool\" : {");
    esQuery.append("        \"must\" : [");
    for (AnalyticsFieldFilter fieldFilter : filters) {
      String esFieldName = fieldFilter.getField();
      StatisticFieldMapping fieldMapping = this.esMappings.get(esFieldName);
      if (fieldMapping != null) {
        esFieldName = fieldMapping.getAggregationFieldName();
      }

      String esQueryValue = fieldMapping == null ? StatisticFieldMapping.computeESQueryValue(fieldFilter.getValueString())
                                                 : fieldMapping.getESQueryValue(fieldFilter.getValueString());
      switch (fieldFilter.getType()) {
        case NOT_NULL:
          esQuery.append("        {\"exists\" : {\"")
                 .append("field")
                 .append("\" : \"")
                 .append(esFieldName)
                 .append("\"      }},");
          break;
        case IS_NULL:
          esQuery.append("        {\"bool\": {\"must_not\": {\"exists\": {\"field\": \"")
                 .append(esFieldName)
                 .append("\"      }}}},");
          break;
        case EQUAL:
          esQuery.append("        {\"match\" : {\"")
                 .append(esFieldName)
                 .append("\" : ")
                 .append(esQueryValue)
                 .append("        }},");
          break;
        case NOT_EQUAL:
          esQuery.append("        {\"bool\": {\"must_not\": {\"match\" : {\"")
                 .append(esFieldName)
                 .append("\" : ")
                 .append(esQueryValue)
                 .append("        }}}},");
          break;
        case GREATER:
          esQuery.append("        {\"range\" : {\"")
                 .append(esFieldName)
                 .append("\" : {")
                 .append("\"gte\" : ")
                 .append(esQueryValue)
                 .append("        }}},");
          break;
        case LESS:
          esQuery.append("        {\"range\" : {\"")
                 .append(esFieldName)
                 .append("\" : {")
                 .append("\"lte\" : ")
                 .append(esQueryValue)
                 .append("        }}},");
          break;
        case RANGE:
          Range range = fieldFilter.getRange();
          esQuery.append("        {\"range\" : {\"")
                 .append(esFieldName)
                 .append("\" : {")
                 .append("\"gte\" : ")
                 .append(range.getMin())
                 .append(",\"lte\" : ")
                 .append(range.getMax())
                 .append("        }}},");
          break;
        case IN_SET:
          esQuery.append("        {\"terms\" : {\"")
                 .append(esFieldName)
                 .append("\" : ")
                 .append(collectionToJSONString(fieldFilter.getValueString()))
                 .append("        }},");
          break;
        case NOT_IN_SET:
          esQuery.append("        {\"bool\": {\"must_not\": {\"terms\" : {\"")
                 .append(esFieldName)
                 .append("\" : ")
                 .append(collectionToJSONString(fieldFilter.getValueString()))
                 .append("        }}}},");
          break;
        default:
          break;
      }
    }
    esQuery.append("        ],");
    esQuery.append("      },");
    esQuery.append("     },");
  }

  private void buildAggregationQuery(StringBuilder esQuery,
                                     List<AnalyticsAggregation> aggregations) {
    if (aggregations != null && !aggregations.isEmpty()) {
      StringBuffer endOfQuery = new StringBuffer();
      for (int i = 0; i < aggregations.size(); i++) {
        AnalyticsAggregation aggregation = aggregations.get(i);

        AnalyticsAggregationType aggregationType = aggregation.getType();
        if (aggregationType.isUseInterval() && StringUtils.isBlank(aggregation.getInterval())) {
          throw new IllegalStateException("Analytics aggregation type '" + aggregationType
              + "' is using intervals while it has empty interval");
        }

        String fieldName = getAggregationFieldName(aggregationType);

        long limit = aggregation.getLimit();
        if (aggregationType.isUseLimit() && limit <= 0) {
          limit = aggregationReturnedDocumentsSize;
        }

        esQuery.append("     ,\"aggs\": {");
        esQuery.append("       \"")
               .append(fieldName)
               .append("\": {");
        esQuery.append("         \"")
               .append(aggregationType.getAggName())
               .append("\": {");
        esQuery.append("           \"field\": \"")
               .append(aggregation.getField())
               .append("\"");

        if (aggregationType.isUseInterval()) {
          esQuery.append(",")
                 .append("           \"interval\": \"")
                 .append(aggregation.getInterval())
                 .append("\"");
        }

        if (aggregationType.isUseLimit() && limit > 0) {
          esQuery.append("           ,\"size\": ").append(limit);
        }
        if (aggregationType.isUseSort() && (i + 1) < aggregations.size()) {
          AnalyticsAggregation nextAggregation = aggregations.get(i + 1);
          String sortField = getSortField(nextAggregation);
          if (sortField != null) {
            esQuery.append(",           \"order\": {\"")
                   .append(sortField)
                   .append("\": \"")
                   .append(aggregation.getSortDirection())
                   .append("\"}");
          }
        }
        esQuery.append("         }");

        // Appended at the end
        endOfQuery.append("       }");
        endOfQuery.append("     }");
      }
      esQuery.append(endOfQuery);
    }
  }

  private String getAggregationFieldName(AnalyticsAggregationType aggregationType) {
    String fieldName = null;
    if (AnalyticsAggregationType.TERMS == aggregationType
        || AnalyticsAggregationType.DATE == aggregationType
        || AnalyticsAggregationType.HISTOGRAM == aggregationType) {
      fieldName = AGGREGATION_RESULT_PARAM;
    } else {
      fieldName = AGGREGATION_RESULT_VALUE_PARAM;
    }
    return fieldName;
  }

  private String getSortField(AnalyticsAggregation nextAggregation) {
    if (nextAggregation == null) {
      return null;
    }

    AnalyticsAggregationType aggType = nextAggregation.getType();
    if (aggType == AnalyticsAggregationType.COUNT
        || aggType == AnalyticsAggregationType.MAX
        || aggType == AnalyticsAggregationType.MIN
        || aggType == AnalyticsAggregationType.AVG
        || aggType == AnalyticsAggregationType.SUM) {
      return getAggregationFieldName(aggType) + ".value";
    }
    return "_count";
  }

  private ChartDataList buildChartDataFromESResponse(AnalyticsFilter filter, String jsonResponse) throws JSONException {
    AnalyticsAggregation multipleChartsAggregation = filter.getMultipleChartsAggregation();
    String lang = filter.getLang();

    ChartDataList chartsData = new ChartDataList(lang);
    JSONObject json = new JSONObject(jsonResponse);
    JSONObject aggregations = json.getJSONObject("aggregations");
    if (aggregations == null) {
      return chartsData;
    }
    JSONObject hitsResult = (JSONObject) json.get("hits");

    chartsData.setComputingTime(json.getLong("took"));
    chartsData.setDataCount(hitsResult.getLong("total"));

    int level = multipleChartsAggregation == null ? 0 : -1;
    computeAggregatedResultEntry(filter, aggregations, chartsData, multipleChartsAggregation, null, null, level);
    addEmptyResultsToNotExistingEntries(chartsData);
    return chartsData;
  }

  private void computeAggregatedResultEntry(AnalyticsFilter filter,
                                            JSONObject aggregations,
                                            ChartDataList chartsData,
                                            AnalyticsAggregation multipleChartsAggregation,
                                            ChartAggregationValue parentAggregation,
                                            ArrayList<ChartAggregationValue> aggregationValues,
                                            int level) throws JSONException {
    String lang = filter.getLang();

    JSONObject aggsResult = aggregations.getJSONObject(AGGREGATION_RESULT_PARAM);
    JSONArray buckets = aggsResult.getJSONArray("buckets");
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
      LOG.error("Error reading ES mapped fields", e);
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

  private void addEmptyResultsToNotExistingEntries(ChartDataList chartsData) {
    LinkedHashSet<ChartAggregationLabel> aggregationLabels = chartsData.getAggregationLabels();
    int index = 0;
    Iterator<ChartAggregationLabel> iterator = aggregationLabels.iterator();
    while (iterator.hasNext()) {
      ChartAggregationLabel chartAggregationLabel = iterator.next();
      // Placeholder result to add to charts not having results retrieved from
      // ES
      ChartAggregationResult emptyResult = new ChartAggregationResult(chartAggregationLabel,
                                                                      chartAggregationLabel.getLabel(),
                                                                      null);
      LinkedHashSet<ChartData> charts = chartsData.getCharts();
      for (ChartData chartData : charts) {
        // Add empty result if not exists on chart at exact same index
        chartData.addAggregationResult(emptyResult, index, false);
      }
      index++;
    }
  }

  private ChartAggregationResult getChartDateAggregationResult(AnalyticsAggregation dateAxisAggregation,
                                                               List<ChartAggregationResult> aggregationResults,
                                                               AnalyticsPeriod period) {
    return aggregationResults.stream().filter(valueAggregationResult -> {
      ChartAggregationLabel chartLabel = valueAggregationResult.retrieveChartLabel();
      if (chartLabel == null || chartLabel.getAggregationValues() == null || chartLabel.getAggregationValues().isEmpty()) {
        return false;
      }
      ChartAggregationValue periodAggregationValue = chartLabel.getAggregationValues()
                                                               .stream()
                                                               .filter(aggregationValue -> dateAxisAggregation
                                                                                                              .equals(aggregationValue.getAggregation()))
                                                               .findFirst()
                                                               .orElse(null);
      if (periodAggregationValue == null) {
        return false;
      }
      long timestamp = Long.parseLong(periodAggregationValue.getFieldValue());
      return timestamp < period.getToInMS() && timestamp >= period.getFromInMS();
    }).findFirst().orElse(null);
  }

}
