package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

import javax.portlet.*;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.*;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.*;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;

public class AnalyticsPortlet extends GenericPortlet {

  private static final String READ_SETTINGS_OPERATOPN      = "GET_SETTINGS";

  private static final String READ_FILTERS_OPERATOPN       = "GET_FILTERS";

  private static final String READ_MAPPINGS_OPERATOPN      = "GET_MAPPINGS";

  private static final String READ_CHART_DATA_OPERATOPN    = "GET_CHART_DATA";

  private static final String READ_CHART_SAMPLES_OPERATOPN = "GET_CHART_SAMPLES_DATA";

  private SpaceService        spaceService;

  private AnalyticsService    analyticsService;

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/analytics.jsp");
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
    response.setPortletMode(PortletMode.VIEW);
  }

  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    PortletPreferences preferences = request.getPreferences();
    PortletSession portletSession = request.getPortletSession();

    String operation = request.getParameter("operation");

    if (StringUtils.equals(operation, READ_SETTINGS_OPERATOPN)) {
      AnalyticsFilter filter = getFilterFromPreferences(preferences, true);
      JSONObject jsonResponse = new JSONObject();
      addJSONParam(jsonResponse, "title", filter.getTitle());
      addJSONParam(jsonResponse, "chartType", filter.getChartType());
      addJSONParam(jsonResponse, "displayComputingTime", filter.isDisplayComputingTime());
      addJSONParam(jsonResponse, "displaySamplesCount", filter.isDisplaySamplesCount());
      addJSONParam(jsonResponse, "canEdit", canModifyChartSettings(portletSession));
      addJSONParam(jsonResponse, "scope", getSearchScope(portletSession).name());
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse.toString());
    } else if (StringUtils.equals(operation, READ_FILTERS_OPERATOPN)) {
      AnalyticsFilter filter = getFilterFromPreferences(preferences, true);
      response.setContentType("application/json");
      response.getWriter().write(AnalyticsUtils.toJsonString(filter));
    } else if (StringUtils.equals(operation, READ_MAPPINGS_OPERATOPN)) {
      Set<StatisticFieldMapping> mappings = getAnalyticsService().retrieveMapping(false);
      Set<JSONObject> objectMappings = mappings.stream().map(mapping -> new JSONObject(mapping)).collect(Collectors.toSet());
      JSONArray jsonArrayResponse = new JSONArray(objectMappings);
      response.setContentType("application/json");
      response.getWriter().write(jsonArrayResponse.toString());
    } else if (StringUtils.equals(operation, READ_CHART_SAMPLES_OPERATOPN)) {
      AnalyticsFilter filter = getFilterFromPreferences(preferences, true);
      addPeriodFilter(request, filter);
      addScopeFilter(portletSession, filter);
      addLanguageFilter(request, filter);
      addLimitFilter(request, filter);

      Object result = getAnalyticsService().retrieveData(filter);
      response.setContentType("application/json");
      response.getWriter().write(AnalyticsUtils.toJsonString(result));
    } else if (StringUtils.equals(operation, READ_CHART_DATA_OPERATOPN)) {
      AnalyticsFilter filter = getFilterFromPreferences(preferences, true);
      addPeriodFilter(request, filter);
      addScopeFilter(portletSession, filter);
      addLanguageFilter(request, filter);

      Object result = getAnalyticsService().compueChartData(filter);
      response.setContentType("application/json");
      response.getWriter().write(AnalyticsUtils.toJsonString(result));
    }
    super.serveResource(request, response);
  }

  private void addLanguageFilter(ResourceRequest request, AnalyticsFilter filter) {
    String lang = request.getParameter("lang");
    filter.setLang(lang);
  }

  private void addLimitFilter(ResourceRequest request, AnalyticsFilter filter) {
    String limitString = request.getParameter("limit");
    if (StringUtils.isBlank(limitString)) {
      limitString = "10";
    }
    filter.setLimit(Long.parseLong(limitString));
  }

  private void addPeriodFilter(ResourceRequest request, AnalyticsFilter filter) {
    String fromDateString = request.getParameter("min");
    String toDateString = request.getParameter("max");
    filter.addRangeFilter("timestamp", fromDateString, toDateString);
  }

  private void addScopeFilter(PortletSession portletSession, AnalyticsFilter filter) throws PortletException {
    SearchScope scope = getSearchScope(portletSession);
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

  private AnalyticsFilter getFilterFromPreferences(PortletPreferences preferences, boolean createNewIfNull) {
    if (preferences != null) {
      String analyticsFilterString = preferences.getValue("settings", null);
      if (StringUtils.isNotBlank(analyticsFilterString)) {
        return AnalyticsUtils.fromJsonString(analyticsFilterString, AnalyticsFilter.class);
      }
    }
    return createNewIfNull ? new AnalyticsFilter() : null;
  }

  private SearchScope getSearchScope(PortletSession portletSession) {
    SearchScope searchScopeFromCache = getSearchScopeFromCache(portletSession);
    if (searchScopeFromCache != null) {
      return searchScopeFromCache;
    }

    ConversationState current = ConversationState.getCurrent();
    if (current == null || current.getIdentity() == null) {
      return cacheSearchScope(portletSession, SearchScope.NONE);
    }
    Identity identity = current.getIdentity();
    String userId = identity.getUserId();
    if (StringUtils.isBlank(userId) || StringUtils.equals(userId, IdentityConstants.ANONIM)
        || StringUtils.equals(userId, IdentityConstants.SYSTEM)) {
      return cacheSearchScope(portletSession, SearchScope.NONE);
    }

    String groupId = getAnalyticsService().getAdministratorsPermission();
    if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
      return cacheSearchScope(portletSession, SearchScope.GLOBAL);
    }

    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      if (getSpaceService().isSuperManager(userId) || getSpaceService().isMember(space, userId)) {
        return cacheSearchScope(portletSession, SearchScope.SPACE);
      } else {
        return cacheSearchScope(portletSession, SearchScope.NONE);
      }
    }

    if (Utils.isOwner() || StringUtil.isBlank(Utils.getOwnerRemoteId())) {
      return cacheSearchScope(portletSession, SearchScope.USER);
    } else {
      return cacheSearchScope(portletSession, SearchScope.NONE);
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
    String groupId = getAnalyticsService().getAdministratorsPermission();
    if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
      return cacheChartModificationAccessPermission(portletSession, true);
    }
    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      boolean canModify = getSpaceService().isSuperManager(userId) || getSpaceService().isManager(space, userId);
      return cacheChartModificationAccessPermission(portletSession, canModify);
    }
    return cacheChartModificationAccessPermission(portletSession, false);
  }

  private Boolean getCanModifySettingsFromCache(PortletSession portletSession) {
    return (Boolean) portletSession.getAttribute("canModifyChartSettings");
  }

  private boolean cacheChartModificationAccessPermission(PortletSession portletSession, boolean canModify) {
    portletSession.setAttribute("canModifyChartSettings", canModify);
    return canModify;
  }

  private SearchScope getSearchScopeFromCache(PortletSession portletSession) {
    return (SearchScope) portletSession.getAttribute("analyticsSearchScope");
  }

  private SearchScope cacheSearchScope(PortletSession portletSession, SearchScope searchScope) {
    portletSession.setAttribute("analyticsSearchScope", searchScope);
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

  enum SearchScope {
    USER,
    SPACE,
    GLOBAL,
    NONE
  }
}
