package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.portlet.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.*;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.chart.TableColumnResult;
import org.exoplatform.analytics.model.filter.*;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.security.*;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;

public class AnalyticsTablePortlet extends GenericPortlet {

  private static final String                            CAN_MODIFY_TABLE_SETTINGS = "canModifyTableSettings";

  private static final String                            ANALYTICS_SEARCH_SCOPE    = "analyticsSearchScope";

  private static final String                            READ_SETTINGS_OPERATION   = "GET_SETTINGS";

  private static final String                            READ_FILTERS_OPERATION    = "GET_FILTERS";

  private static final String                            READ_MAPPINGS_OPERATION   = "GET_MAPPINGS";

  private static final String                            READ_TABLE_DATA_OPERATION = "GET_TABLE_DATA";

  private static final Map<String, AnalyticsTableFilter> FILTERS                   = new HashMap<>();

  private SpaceService                                   spaceService;

  private AnalyticsService                               analyticsService;

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/analytics-table.jsp");
    dispatcher.forward(request, response);
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifyTableSettings(request.getPortletSession())) {
      throw new PortletException("User is not allowed to save table settings");
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
      AnalyticsTableFilter filter = getFilterFromPreferences(windowId, preferences, false);
      JSONObject jsonResponse = new JSONObject();
      addJSONParam(jsonResponse, "title", filter.getTitle());
      addJSONParam(jsonResponse, "canEdit", canModifyTableSettings(portletSession));
      addJSONParam(jsonResponse, "scope", getSearchScope(portletSession).name());
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse.toString());
    } else if (StringUtils.equals(operation, READ_FILTERS_OPERATION)) {
      AnalyticsTableFilter filter = getFilterFromPreferences(windowId, preferences, false);
      response.setContentType("application/json");
      response.getWriter().write(AnalyticsUtils.toJsonString(filter));
    } else if (StringUtils.equals(operation, READ_MAPPINGS_OPERATION)) {
      Set<StatisticFieldMapping> mappings = getAnalyticsService().retrieveMapping(false);
      List<JSONObject> objectMappings = mappings.stream().map(JSONObject::new).collect(Collectors.toList());
      JSONArray jsonArrayResponse = new JSONArray(objectMappings);
      response.setContentType("application/json");
      response.getWriter().write(jsonArrayResponse.toString());
    } else if (StringUtils.equals(operation, READ_TABLE_DATA_OPERATION)) {
      AnalyticsTableFilter tableFilter = getFilterFromPreferences(windowId, preferences, true);
      if (tableFilter == null || tableFilter.getMainColumn() == null
          || tableFilter.getMainColumn().getValueAggregation() == null
          || tableFilter.getMainColumn().getValueAggregation().getAggregation() == null
          || tableFilter.getMainColumn().getValueAggregation().getAggregation().getField() == null) {
        response.setContentType(MediaType.APPLICATION_JSON);
        response.getWriter().write("{}");
        return;
      }
      String column = request.getParameter("column");
      int columnIndex = 0;
      if (StringUtils.isNotBlank(column)) {
        columnIndex = Integer.parseInt(column);
      }
      String fromDateString = request.getParameter("min");
      String toDateString = request.getParameter("max");
      AnalyticsPeriod period = new AnalyticsPeriod(Long.parseLong(fromDateString), Long.parseLong(toDateString));
      AnalyticsPeriodType periodType = null;
      String analyticsPeriodType = request.getParameter("periodType");
      if (StringUtils.isNotBlank(analyticsPeriodType)) {
        periodType = AnalyticsPeriodType.periodTypeByName(analyticsPeriodType);
        period = tableFilter.getCurrentPeriod(period, periodType);
      }
      AnalyticsFieldFilter fieldFilter = null;

      String fieldFilterName = request.getParameter("fieldFilter");
      String fieldFilterValues = request.getParameter("fieldValues");
      if (StringUtils.isNotBlank(fieldFilterName) && StringUtils.isNotBlank(fieldFilterValues)) {
        fieldFilter = new AnalyticsFieldFilter(fieldFilterName, AnalyticsFieldFilterType.IN_SET, fieldFilterValues);
      }

      int limit;
      try {
        limit = Integer.parseInt(request.getParameter("limit"));
      } catch (NumberFormatException e) {
        limit = 0;
      }
      String sort = request.getParameter("sort");

      AnalyticsFilter filter = tableFilter.buildColumnFilter(period,
                                                             periodType,
                                                             fieldFilter,
                                                             limit,
                                                             sort,
                                                             columnIndex,
                                                             true);
      addScopeFilter(portletSession, filter);
      addLanguageFilter(request, filter);

      TableColumnResult result = getAnalyticsService().computeTableColumnData(null,
                                                                              tableFilter,
                                                                              filter,
                                                                              period,
                                                                              periodType,
                                                                              columnIndex,
                                                                              true);
      AnalyticsTableColumnFilter columnFilter = tableFilter.getColumnFilter(columnIndex);
      if (columnFilter.getThresholdAggregation() != null
          && columnFilter.getThresholdAggregation().getAggregation() != null
          && columnFilter.getThresholdAggregation().getAggregation().getType() != null
          && columnFilter.getThresholdAggregation().getAggregation().getField() != null) {
        filter = tableFilter.buildColumnFilter(period,
                                               periodType,
                                               fieldFilter,
                                               limit,
                                               sort,
                                               columnIndex,
                                               false);
        addScopeFilter(portletSession, filter);
        addLanguageFilter(request, filter);

        getAnalyticsService().computeTableColumnData(result,
                                                     tableFilter,
                                                     filter,
                                                     period,
                                                     periodType,
                                                     columnIndex,
                                                     false);
      }

      response.setContentType(MediaType.APPLICATION_JSON);
      response.getWriter().write(AnalyticsUtils.toJsonString(result));
    }
    super.serveResource(request, response);
  }

  private void addLanguageFilter(ResourceRequest request, AnalyticsFilter filter) {
    String lang = request.getParameter("lang");
    filter.setLang(lang);
  }

  private void addScopeFilter(PortletSession portletSession, AnalyticsFilter filter) throws PortletException {
    AnalyticsPortlet.SearchScope scope = getSearchScope(portletSession);
    switch (scope) {
      case GLOBAL:
        break;
      case NONE:
        throw new PortletException("Not allowed to access information");
      case SPACE:
        Space space = SpaceUtils.getSpaceByContext();
        filter.addEqualFilter("spaceId", space.getId());
        break;
      case USER:
        String viewerIdentityId = Utils.getViewerIdentity().getId();
        filter.addEqualFilter("userId", viewerIdentityId);
        break;
    }
  }

  private AnalyticsTableFilter getFilterFromPreferences(String windowId, PortletPreferences preferences, boolean clone) {
    AnalyticsTableFilter filter = getAnlyticsFilterCache(windowId);

    if (filter == null) {
      if (preferences != null) {
        String analyticsFilterString = preferences.getValue("settings", null);
        if (StringUtils.isNotBlank(analyticsFilterString)) {
          filter = AnalyticsUtils.fromJsonString(analyticsFilterString, AnalyticsTableFilter.class);
        }
      }
      if (filter == null) {
        filter = new AnalyticsTableFilter();
      }
      setAnlyticsFilterCache(windowId, filter);
    }
    return clone ? filter.clone() : filter;
  }

  private AnalyticsPortlet.SearchScope getSearchScope(PortletSession portletSession) {
    AnalyticsPortlet.SearchScope searchScopeFromCache = getSearchScopeFromCache(portletSession);
    if (searchScopeFromCache != null) {
      return searchScopeFromCache;
    }

    ConversationState current = ConversationState.getCurrent();
    if (current == null || current.getIdentity() == null) {
      return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.NONE);
    }
    Identity identity = current.getIdentity();
    String userId = identity.getUserId();
    if (StringUtils.isBlank(userId) || StringUtils.equals(userId, IdentityConstants.ANONIM)
        || StringUtils.equals(userId, IdentityConstants.SYSTEM)) {
      return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.NONE);
    }

    List<String> groupIds = getAnalyticsService().getAdministratorsPermissions();
    for (String groupId : groupIds) {
      if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
        return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.GLOBAL);
      }
    }

    groupIds = getAnalyticsService().getViewAllPermissions();
    for (String groupId : groupIds) {
      if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
        return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.GLOBAL);
      }
    }

    groupIds = getAnalyticsService().getViewPermissions();
    boolean canView = false;
    for (String groupId : groupIds) {
      if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
        canView = true;
        break;
      }
    }

    if (!canView) {
      return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.NONE);
    }

    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      if (getSpaceService().isSuperManager(userId) || getSpaceService().isMember(space, userId)) {
        return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.SPACE);
      } else {
        return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.NONE);
      }
    }

    if (Utils.isOwner() || StringUtil.isBlank(Utils.getOwnerRemoteId())) {
      return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.USER);
    } else {
      return cacheSearchScope(portletSession, AnalyticsPortlet.SearchScope.NONE);
    }
  }

  private boolean canModifyTableSettings(PortletSession portletSession) {
    Boolean canModifyFromCache = getCanModifySettingsFromCache(portletSession);
    if (canModifyFromCache != null) {
      return canModifyFromCache;
    }
    ConversationState current = ConversationState.getCurrent();
    if (current == null || current.getIdentity() == null) {
      return cacheTableModificationAccessPermission(portletSession, false);
    }
    Identity identity = current.getIdentity();
    String userId = identity.getUserId();
    if (StringUtils.isBlank(userId) || StringUtils.equals(userId, IdentityConstants.ANONIM)
        || StringUtils.equals(userId, IdentityConstants.SYSTEM)) {
      return cacheTableModificationAccessPermission(portletSession, false);
    }
    List<String> groupIds = getAnalyticsService().getAdministratorsPermissions();
    for (String groupId : groupIds) {
      if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
        return cacheTableModificationAccessPermission(portletSession, true);
      }
    }
    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      boolean canModify = getSpaceService().isSuperManager(userId) || getSpaceService().isManager(space, userId);
      return cacheTableModificationAccessPermission(portletSession, canModify);
    }
    return cacheTableModificationAccessPermission(portletSession, false);
  }

  private Boolean getCanModifySettingsFromCache(PortletSession portletSession) {
    return (Boolean) portletSession.getAttribute(CAN_MODIFY_TABLE_SETTINGS);
  }

  private boolean cacheTableModificationAccessPermission(PortletSession portletSession, boolean canModify) {
    if (!PropertyManager.isDevelopping()) {
      portletSession.setAttribute(CAN_MODIFY_TABLE_SETTINGS, canModify);
    }
    return canModify;
  }

  private AnalyticsPortlet.SearchScope getSearchScopeFromCache(PortletSession portletSession) {
    return (AnalyticsPortlet.SearchScope) portletSession.getAttribute(ANALYTICS_SEARCH_SCOPE);
  }

  private AnalyticsPortlet.SearchScope cacheSearchScope(PortletSession portletSession, AnalyticsPortlet.SearchScope searchScope) {
    if (!PropertyManager.isDevelopping()) {
      portletSession.setAttribute(ANALYTICS_SEARCH_SCOPE, searchScope);
    }
    return searchScope;
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

  private void setAnlyticsFilterCache(String windowId, AnalyticsTableFilter filter) {
    if (filter == null) {
      clearAnlyticsFilterCache(windowId);
    } else {
      FILTERS.put(windowId, filter);
    }
  }

  private AnalyticsTableFilter getAnlyticsFilterCache(String windowId) {
    AnalyticsTableFilter analyticsTableFilter = FILTERS.get(windowId);
    return analyticsTableFilter == null ? null : analyticsTableFilter.clone();
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
