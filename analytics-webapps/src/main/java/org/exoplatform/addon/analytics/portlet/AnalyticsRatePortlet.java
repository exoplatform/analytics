package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.portlet.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.json.*;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.StatisticFieldValue;
import org.exoplatform.analytics.model.filter.*;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.security.*;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class AnalyticsRatePortlet extends GenericPortlet {

  private static final String                                 CAN_MODIFY_CHART_SETTINGS   = "canModifyChartSettings";

  private static final String                                 READ_SETTINGS_OPERATION     = "GET_SETTINGS";

  private static final String                                 READ_FILTERS_OPERATION      = "GET_FILTERS";

  private static final String                                 READ_MAPPINGS_OPERATION     = "GET_MAPPINGS";

  private static final String                                 READ_CHART_DATA_OPERATION   = "GET_CHART_DATA";

  private static final String                                 READ_FIELD_VALUES_OPERATION = "GET_FIELD_VALUES";

  private static final Map<String, AnalyticsPercentageFilter> FILTERS                     = new HashMap<>();

  private SpaceService                                        spaceService;

  private AnalyticsService                                    analyticsService;

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/analytics-rate.jsp");
    dispatcher.forward(request, response);
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifyChartSettings(request.getPortletSession())) {
      throw new PortletException("User is not allowed to save chart settings");
    }

    PortletPreferences preferences = request.getPreferences();
    Enumeration<String> parameterNames = request.getParameterNames();
    if (parameterNames != null && parameterNames.hasMoreElements()) {
      while (parameterNames.hasMoreElements()) {
        String name = parameterNames.nextElement();
        String paramValue = request.getParameter(name);
        preferences.setValue(name, paramValue);
      }
    }
    preferences.store();
    clearAnlyticsFilterCache(request.getWindowID());
    response.setPortletMode(PortletMode.VIEW);
  }

  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    PortletPreferences preferences = request.getPreferences();
    PortletSession portletSession = request.getPortletSession();

    String operation = request.getParameter("operation");

    String windowId = request.getWindowID();
    if (StringUtils.equals(operation, READ_SETTINGS_OPERATION)) {
      AnalyticsPercentageFilter filter = getFilterFromPreferences(windowId, preferences, false);
      JSONObject jsonResponse = new JSONObject();
      addJSONParam(jsonResponse, "title", filter.getTitle());
      addJSONParam(jsonResponse, "chartType", filter.getChartType());
      addJSONParam(jsonResponse, "colors", filter.getColors());
      addJSONParam(jsonResponse, "canEdit", canModifyChartSettings(portletSession));
      response.setContentType(MediaType.APPLICATION_JSON);
      response.getWriter().write(jsonResponse.toString());
    } else if (StringUtils.equals(operation, READ_FILTERS_OPERATION)) {
      AnalyticsPercentageFilter filter = getFilterFromPreferences(windowId, preferences, false);
      response.setContentType(MediaType.APPLICATION_JSON);
      response.getWriter().write(AnalyticsUtils.toJsonString(filter));
    } else if (StringUtils.equals(operation, READ_MAPPINGS_OPERATION)) {
      Set<StatisticFieldMapping> mappings = getAnalyticsService().retrieveMapping(false);
      List<JSONObject> objectMappings = mappings.stream().map(JSONObject::new).collect(Collectors.toList());
      JSONArray jsonArrayResponse = new JSONArray(objectMappings);
      response.setContentType(MediaType.APPLICATION_JSON);
      response.getWriter().write(jsonArrayResponse.toString());
    } else if (StringUtils.equals(operation, READ_CHART_DATA_OPERATION)) {
      AnalyticsPercentageFilter filter = getFilterFromPreferences(windowId, preferences, true);
      addLanguageFilter(request, filter);
      addPeriodFilter(request, filter);
      Object result = getAnalyticsService().computePercentageChartData(filter);
      response.setContentType(MediaType.APPLICATION_JSON);
      response.getWriter().write(AnalyticsUtils.toJsonString(result));
    } else if (StringUtils.equals(operation, READ_FIELD_VALUES_OPERATION)) {
      String field = request.getParameter("field");
      String limitString = request.getParameter("limit");
      int limit = StringUtils.isBlank(limitString) ? 10 : Integer.parseInt(limitString);
      if (StringUtils.isNotBlank(field)) {
        List<StatisticFieldValue> fieldValues = getAnalyticsService().retrieveFieldValues(field, limit);
        response.getWriter().write(AnalyticsUtils.toJsonString(fieldValues));
        response.setContentType("application/json");
      } else {
        response.getWriter().write("[]");
        response.setContentType("application/json");
      }
    }
    super.serveResource(request, response);
  }

  private void addLanguageFilter(ResourceRequest request, AnalyticsPercentageFilter filter) {
    String lang = request.getParameter("lang");
    filter.setLang(lang);
  }

  private void addPeriodFilter(ResourceRequest request, AnalyticsPercentageFilter filter) {
    String period = request.getParameter("period");
    if (AnalyticsPeriodType.periodTypeByName(period) == null) {
      if (StringUtils.contains(period, "~")) {
        String[] periodDates = period.split("~");
        filter.setCustomPeriod(new AnalyticsPeriod(Long.parseLong(periodDates[0].trim()),
                                                   Long.parseLong(periodDates[1].trim())));
      }
    } else {
      String periodDateString = request.getParameter("date");
      filter.setPeriodDateInMS(Long.parseLong(periodDateString));
      filter.setPeriodType(period);
    }
  }

  private AnalyticsPercentageFilter getFilterFromPreferences(String windowId, PortletPreferences preferences, boolean clone) {
    AnalyticsPercentageFilter filter = getAnlyticsFilterCache(windowId);

    if (filter == null) {
      if (preferences != null) {
        String analyticsFilterString = preferences.getValue("settings", null);
        if (StringUtils.isNotBlank(analyticsFilterString)) {
          filter = AnalyticsUtils.fromJsonString(analyticsFilterString, AnalyticsPercentageFilter.class);
        }
      }
      if (filter == null) {
        filter = new AnalyticsPercentageFilter();
      }
      setAnlyticsFilterCache(windowId, filter);
    }
    return clone ? filter.clone() : filter;
  }

  private boolean canModifyChartSettings(PortletSession portletSession) {
    Boolean canModifyFromCache = getCanModifySettingsFromCache(portletSession);
    if (canModifyFromCache != null) {
      return canModifyFromCache;
    }
    ConversationState current = ConversationState.getCurrent();
    if (current == null || current.getIdentity() == null) {
      return cacheChartModificationAccessPermission(portletSession, false);
    }
    Identity identity = current.getIdentity();
    String userId = identity.getUserId();
    if (StringUtils.isBlank(userId) || StringUtils.equals(userId, IdentityConstants.ANONIM)
        || StringUtils.equals(userId, IdentityConstants.SYSTEM)) {
      return cacheChartModificationAccessPermission(portletSession, false);
    }
    List<String> groupIds = getAnalyticsService().getAdministratorsPermissions();
    for (String groupId : groupIds) {
      if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
        return cacheChartModificationAccessPermission(portletSession, true);
      }
    }
    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      boolean canModify = getSpaceService().isSuperManager(userId) || getSpaceService().isManager(space, userId);
      return cacheChartModificationAccessPermission(portletSession, canModify);
    }
    return cacheChartModificationAccessPermission(portletSession, false);
  }

  private Boolean getCanModifySettingsFromCache(PortletSession portletSession) {
    return (Boolean) portletSession.getAttribute(CAN_MODIFY_CHART_SETTINGS);
  }

  private boolean cacheChartModificationAccessPermission(PortletSession portletSession, boolean canModify) {
    if (!PropertyManager.isDevelopping()) {
      portletSession.setAttribute(CAN_MODIFY_CHART_SETTINGS, canModify);
    }
    return canModify;
  }

  private void addJSONParam(JSONObject jsonResponse, String paramName, Object paramValue) throws PortletException {
    try {
      jsonResponse.put(paramName, paramValue);
    } catch (JSONException e) {
      throw new PortletException("Error adding parameter to JSON", e);
    }
  }

  private AnalyticsService getAnalyticsService() {
    if (analyticsService == null) {
      analyticsService = CommonsUtils.getService(AnalyticsService.class);
    }
    return analyticsService;
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = CommonsUtils.getService(SpaceService.class);
    }
    return spaceService;
  }

  private void setAnlyticsFilterCache(String windowId, AnalyticsPercentageFilter filter) {
    if (filter == null) {
      clearAnlyticsFilterCache(windowId);
    } else {
      FILTERS.put(windowId, filter);
    }
  }

  private AnalyticsPercentageFilter getAnlyticsFilterCache(String windowId) {
    AnalyticsPercentageFilter analyticsPercentageFilter = FILTERS.get(windowId);
    return analyticsPercentageFilter == null ? null : analyticsPercentageFilter.clone();
  }

  private void clearAnlyticsFilterCache(String windowId) {
    FILTERS.remove(windowId);
  }

  enum SearchScope {
    USER,
    SPACE,
    GLOBAL,
    NONE
  }
}
