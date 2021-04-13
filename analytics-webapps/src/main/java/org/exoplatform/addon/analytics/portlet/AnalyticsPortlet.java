package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.portlet.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.analytics.utils.AnalyticsUtils;

public class AnalyticsPortlet extends AbstractAnalyticsPortlet<AnalyticsFilter> {

  @Override
  protected String getViewPagePath() {
    return "/WEB-INF/jsp/analytics.jsp";
  }

  @Override
  protected Class<AnalyticsFilter> getFilterClass() {
    return AnalyticsFilter.class;
  }

  @Override
  protected void readSettings(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    AnalyticsFilter filter = getFilterFromPreferences(request);
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().write(AnalyticsUtils.toJsonString(filter));
  }

  @Override
  protected void readSettingsReadOnly(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    AnalyticsFilter filter = getFilterFromPreferences(request);
    JSONObject jsonResponse = new JSONObject();
    addJSONParam(jsonResponse, "title", filter.getTitle());
    addJSONParam(jsonResponse, "chartType", filter.getChartType());
    List<String> colors = filter.getColors() == null ? Collections.emptyList() : filter.getColors();
    addJSONParam(jsonResponse, "colors", new JSONArray(colors));
    addJSONParam(jsonResponse, "canEdit", canModifySettings(request));
    addJSONParam(jsonResponse, "scope", getSearchScope(request).name());
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().write(jsonResponse.toString());
  }

  @Override
  protected void readSamples(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    AnalyticsFilter filter = getFilterFromPreferences(request);
    addPeriodFilter(request, filter);
    addScopeFilter(request, filter);
    addLanguageFilter(request, filter);
    addLimitFilter(request, filter);
    addTimeZoneFilter(request, filter);
    addSortFilter(filter, "desc");

    List<StatisticData> statisticDatas = getAnalyticsService().retrieveData(filter);
    List<JSONObject> objectMappings = statisticDatas.stream()
                                                    .map(statisticData -> {
                                                      JSONObject object = new JSONObject(statisticData);
                                                      object.remove("class");
                                                      return object;
                                                    })
                                                    .collect(Collectors.toList());
    JSONArray jsonArrayResponse = new JSONArray(objectMappings);
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().write(jsonArrayResponse.toString());
  }

  @Override
  protected void readData(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    AnalyticsFilter filter = getFilterFromPreferences(request);
    addPeriodFilter(request, filter);
    addScopeFilter(request, filter);
    addLanguageFilter(request, filter);
    addTimeZoneFilter(request, filter);

    Object result = getAnalyticsService().computeChartData(filter);
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().write(AnalyticsUtils.toJsonString(result));
  }

}
