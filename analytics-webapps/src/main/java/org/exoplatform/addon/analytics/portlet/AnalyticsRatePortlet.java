package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

import javax.portlet.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import org.exoplatform.analytics.model.filter.*;
import org.exoplatform.analytics.utils.AnalyticsUtils;

public class AnalyticsRatePortlet extends AbstractAnalyticsPortlet<AnalyticsPercentageFilter> {

  protected String getViewPagePath() {
    return "/WEB-INF/jsp/analytics-rate.jsp";
  }

  @Override
  protected Class<AnalyticsPercentageFilter> getFilterClass() {
    return AnalyticsPercentageFilter.class;
  }

  @Override
  protected void readSettingsReadOnly(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    AnalyticsPercentageFilter filter = getFilterFromPreferences(request);
    JSONObject jsonResponse = new JSONObject();
    addJSONParam(jsonResponse, "title", filter.getTitle());
    addJSONParam(jsonResponse, "chartType", filter.getChartType());
    addJSONParam(jsonResponse, "colors", filter.getColors());
    addJSONParam(jsonResponse, "canEdit", canModifySettings(request));
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().write(jsonResponse.toString());
  }

  @Override
  protected void readSettings(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    AnalyticsPercentageFilter filter = getFilterFromPreferences(request);
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().write(AnalyticsUtils.toJsonString(filter));
  }

  @Override
  protected void readData(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    AnalyticsPercentageFilter filter = getFilterFromPreferences(request);
    addLanguageFilter(request, filter);
    addTimeZoneFilter(request, filter);
    addPeriodFilter(request, filter);
    Object result = getAnalyticsService().computePercentageChartData(filter);
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().write(AnalyticsUtils.toJsonString(result));
  }

  private void addLanguageFilter(ResourceRequest request, AnalyticsPercentageFilter filter) {
    String lang = request.getParameter("lang");
    filter.setLang(lang);
  }

  private void addPeriodFilter(ResourceRequest request, AnalyticsPercentageFilter filter) {
    String fromDateString = request.getParameter("min");
    String toDateString = request.getParameter("max");
    String periodType = request.getParameter("periodType");

    AnalyticsPeriod period = new AnalyticsPeriod(Long.parseLong(fromDateString), Long.parseLong(toDateString), filter.zoneId());
    if (StringUtils.isNotBlank(periodType)) {
      AnalyticsPeriodType analyticsPeriodType = AnalyticsPeriodType.periodTypeByName(periodType);
      long middleOfPeriod = (period.getFromInMS() + period.getToInMS()) / 2;
      LocalDate middlePeriodDate = Instant.ofEpochMilli(middleOfPeriod).atZone(filter.zoneId()).toLocalDate();
      period = analyticsPeriodType.getCurrentPeriod(middlePeriodDate, filter.zoneId());
      middleOfPeriod = (period.getFromInMS() + period.getToInMS()) / 2;
      filter.setPeriodDateInMS(middleOfPeriod);
      filter.setPeriodType(periodType);
    } else {
      filter.setCustomPeriod(period);
    }
  }
}
