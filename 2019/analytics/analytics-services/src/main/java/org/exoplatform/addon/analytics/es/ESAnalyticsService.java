package org.exoplatform.addon.analytics.es;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.*;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.*;

import org.exoplatform.addon.analytics.api.service.AnalyticsQueueService;
import org.exoplatform.addon.analytics.api.service.AnalyticsService;
import org.exoplatform.addon.analytics.model.*;
import org.exoplatform.addon.analytics.model.chart.ChartType;
import org.exoplatform.addon.analytics.model.chart.LineChartData;
import org.exoplatform.addon.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;

public class ESAnalyticsService extends AnalyticsService {
  // private static final Log LOG =
  // ExoLogger.getLogger(ESAnalyticsService.class);

  private List<String>           esKnownDataFields = null;

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
  public LineChartData getChartData(ChartType chartType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<StatisticData> getData(AnalyticsFilter filter) {
    StringBuilder esQuery = buildFilterQuery(filter, false);
    String jsonResponse = this.esClient.sendRequest(esQuery.toString(),
                                                    AnalyticsIndexingServiceConnector.ES_ALIAS,
                                                    AnalyticsIndexingServiceConnector.ES_TYPE);
    try {
      return buildResult(jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with filter: " + filter + ", response: " + jsonResponse, e);
    }
  }

  @Override
  public int count(AnalyticsFilter filter) {
    StringBuilder esQuery = buildFilterQuery(filter, true);
    String esQueryString = esQuery.toString();
    esQueryString = fixJSONStringFormat(esQueryString);
    String jsonResponse = this.esClient.sendRequest(esQueryString,
                                                    AnalyticsIndexingServiceConnector.ES_ALIAS,
                                                    AnalyticsIndexingServiceConnector.ES_TYPE);
    try {
      return getCount(jsonResponse);
    } catch (JSONException e) {
      throw new IllegalStateException("Error parsing results with filter: " + filter + ", response: " + jsonResponse, e);
    }
  }

  private StringBuilder buildFilterQuery(AnalyticsFilter filter, boolean isCount) {
    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");

    // If it's a count query, no need for results and no need for sort neither
    if (isCount) {
      esQuery.append("     \"size\" : 0");
    } else {
      // Offset
      long offset = filter == null ? 0 : filter.getOffset();
      if (offset > 0) {
        esQuery.append("     \"from\" : ").append(offset).append(",\n");
      }
      // Limit
      long limit = filter == null ? 10 : filter.getLimit();
      if (limit >= 0 && limit < Long.MAX_VALUE) {
        limit = 10;
      }
      esQuery.append("     \"size\" : ").append(limit).append(",\n");
      // Sort by date
      esQuery.append("     \"sort\" : [{ \"timestamp\":{\"order\" : \"desc\"}}]");
    }

    // Query body
    appendFilterConditions(filter, esQuery);
    // End query body

    esQuery.append("}");
    return esQuery;
  }

  private void appendFilterConditions(AnalyticsFilter filter, StringBuilder esQuery) {
    if (filter != null && !filter.getFilters().isEmpty()) {
      esQuery.append(",\n");
      List<AnalyticsFieldFilter> filters = filter.getFilters();
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

    // TODO in a Set of values guery // NOSONAR
  }

  private List<StatisticData> buildResult(String jsonResponse) throws JSONException {
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

  private int getCount(String jsonResponse) throws JSONException {
    JSONObject json = new JSONObject(jsonResponse);
    JSONObject jsonResult = json.getJSONObject("hits");
    if (jsonResult == null)
      return 0;
    return jsonResult.getInt("total");
  }

}
