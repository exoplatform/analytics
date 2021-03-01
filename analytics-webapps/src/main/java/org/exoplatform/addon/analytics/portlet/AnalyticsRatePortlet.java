package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.portlet.*;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.*;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.filter.AnalyticsPercentageFilter;
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

public class AnalyticsRatePortlet extends GenericPortlet {

  private static final String                                 CAN_MODIFY_CHART_SETTINGS = "canModifyChartSettings";

  private static final String                                 ANALYTICS_SEARCH_SCOPE    = "analyticsSearchScope";

  private static final String                                 READ_SETTINGS_OPERATION   = "GET_SETTINGS";

  private static final String                                 READ_FILTERS_OPERATION    = "GET_FILTERS";

  private static final String                                 READ_MAPPINGS_OPERATION   = "GET_MAPPINGS";

  private static final String                                 READ_CHART_DATA_OPERATION = "GET_CHART_DATA";

  private static final Map<String, AnalyticsPercentageFilter> FILTERS                   = new HashMap<>();

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
      addJSONParam(jsonResponse, "color", filter.getColor());
      addJSONParam(jsonResponse, "canEdit", canModifyChartSettings(portletSession));
      addJSONParam(jsonResponse, "scope", getSearchScope(portletSession).name());
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse.toString());
    } else if (StringUtils.equals(operation, READ_FILTERS_OPERATION)) {
      AnalyticsPercentageFilter filter = getFilterFromPreferences(windowId, preferences, false);
      response.setContentType("application/json");
      response.getWriter().write(AnalyticsUtils.toJsonString(filter));
    } else if (StringUtils.equals(operation, READ_MAPPINGS_OPERATION)) {
      Set<StatisticFieldMapping> mappings = getAnalyticsService().retrieveMapping(false);
      List<JSONObject> objectMappings = mappings.stream().map(mapping -> new JSONObject(mapping)).collect(Collectors.toList());
      JSONArray jsonArrayResponse = new JSONArray(objectMappings);
      response.setContentType("application/json");
      response.getWriter().write(jsonArrayResponse.toString());
    } else if (StringUtils.equals(operation, READ_CHART_DATA_OPERATION)) {
      AnalyticsPercentageFilter filter = getFilterFromPreferences(windowId, preferences, true);
      addLanguageFilter(request, filter);
      addPeriodFilter(request, filter);
      addScopeFilter(portletSession, filter);

      Object result = getAnalyticsService().computeChartData(filter);
      response.setContentType("application/json");
      response.getWriter().write(AnalyticsUtils.toJsonString(result));
    }
    super.serveResource(request, response);
  }

  private void addLanguageFilter(ResourceRequest request, AnalyticsPercentageFilter filter) {
    String lang = request.getParameter("lang");
    filter.setLang(lang);
  }

  private void addPeriodFilter(ResourceRequest request, AnalyticsPercentageFilter filter) {
    String fromDateString = request.getParameter("min");
    String toDateString = request.getParameter("max");
    filter.setFromDateInMS(Long.parseLong(fromDateString));
    filter.setToDateInMS(Long.parseLong(toDateString));
  }

  private void addScopeFilter(PortletSession portletSession, AnalyticsPercentageFilter filter) throws PortletException {
    AnalyticsPortlet.SearchScope scope = getSearchScope(portletSession);
    switch (scope) {
      case GLOBAL:
        break;
      case NONE:
        throw new PortletException("Not allowed to access information");
      case SPACE:
        Space space = SpaceUtils.getSpaceByContext();
        AnalyticsFieldFilter spaceScopeFilter =
                                              new AnalyticsFieldFilter("spaceId", AnalyticsFieldFilterType.EQUAL, space.getId());
        filter.setScopeFilter(spaceScopeFilter);
        break;
      case USER:
        String viewerIdentityId = Utils.getViewerIdentity().getId();
        AnalyticsFieldFilter userScopeFilter =
                                             new AnalyticsFieldFilter("userId", AnalyticsFieldFilterType.EQUAL, viewerIdentityId);
        filter.setScopeFilter(userScopeFilter);
        break;
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
