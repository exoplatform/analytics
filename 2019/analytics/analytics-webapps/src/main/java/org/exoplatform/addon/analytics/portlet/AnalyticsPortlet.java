package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.*;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

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

  private static final String READ_CHART_DATA_OPERATOPN    = "GET_CHART_DATA";

  private static final String READ_CHART_SAMPLES_OPERATOPN = "GET_CHART_SAMPLES_DATA";

  private SpaceService        spaceService;

  private AnalyticsService    analyticsService;

  private SearchScope         searchScope;

  private Boolean             canModifyChartSettings;

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/analytics.jsp");
    dispatcher.forward(request, response);
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifyChartSettings()) {
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

    String operation = request.getParameter("Operation");

    if (StringUtils.equals(operation, READ_SETTINGS_OPERATOPN)) {
      JSONObject jsonResponse = new JSONObject();
      addJSONParam(jsonResponse, "canEdit", canModifyChartSettings());
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse.toString());
    } else if (StringUtils.equals(operation, READ_FILTERS_OPERATOPN)) {
      JSONObject jsonResponse = new JSONObject();
      addFilterPreferencesToJSON(preferences, jsonResponse);
      Set<StatisticFieldMapping> mappings = getAnalyticsService().retrieveMapping(false);
      if (mappings != null) {
        addJSONParam(jsonResponse, "mapping", mappings);
      }
      response.setContentType("application/json");
      response.getWriter().write(jsonResponse.toString());
    } else if (StringUtils.equals(operation, READ_CHART_DATA_OPERATOPN)
        || StringUtils.equals(operation, READ_CHART_SAMPLES_OPERATOPN)) {
      JSONObject analyticsFilterJSON = new JSONObject();
      addFilterPreferencesToJSON(preferences, analyticsFilterJSON);
      AnalyticsFilter filter = AnalyticsUtils.fromJsonString(analyticsFilterJSON.toString(), AnalyticsFilter.class);

      SearchScope scope = getSearchScope();
      switch (scope) {
      case GLOBAL:
        break;
      case NONE:
        return;
      case SPACE:
        Space space = SpaceUtils.getSpaceByContext();
        filter.addEqualFilter("spaceId", space.getId());
        break;
      case USER:
        String viewerIdentityId = Utils.getViewerIdentity().getId();
        filter.addEqualFilter("userId", viewerIdentityId);
        break;
      }

      Object result = null;
      if (StringUtils.equals(operation, READ_CHART_DATA_OPERATOPN)) {
        result = getAnalyticsService().compueChartData(filter);
      } else {
        result = getAnalyticsService().retrieveData(filter);
      }
      response.setContentType("application/json");
      response.getWriter().write(AnalyticsUtils.toJsonString(result));
    }
    super.serveResource(request, response);
  }

  private void addFilterPreferencesToJSON(PortletPreferences preferences, JSONObject jsonResponse) throws PortletException {
    if (preferences != null) {
      Set<Entry<String, String[]>> preferencesEntries = preferences.getMap().entrySet();
      for (Entry<String, String[]> entry : preferencesEntries) {
        addJSONParam(jsonResponse,
                     entry.getKey(),
                     entry.getValue() == null || entry.getValue().length == 0 ? "" : entry.getValue()[0]);
      }
    }
  }

  private SearchScope getSearchScope() {
    if (searchScope != null) {
      return searchScope;
    }

    ConversationState current = ConversationState.getCurrent();
    if (current == null || current.getIdentity() == null) {
      searchScope = SearchScope.NONE;
      return searchScope;
    }
    Identity identity = current.getIdentity();
    String userId = identity.getUserId();
    if (StringUtils.isBlank(userId) || StringUtils.equals(userId, IdentityConstants.ANONIM)
        || StringUtils.equals(userId, IdentityConstants.SYSTEM)) {
      searchScope = SearchScope.NONE;
      return searchScope;
    }

    String groupId = getAnalyticsService().getAdministratorsPermission();
    if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
      searchScope = SearchScope.GLOBAL;
      return searchScope;
    }

    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      if (getSpaceService().isSuperManager(userId) || getSpaceService().isMember(space, userId)) {
        searchScope = SearchScope.SPACE;
        return searchScope;
      } else {
        searchScope = SearchScope.NONE;
        return searchScope;
      }
    }

    if (Utils.isOwner() || StringUtil.isBlank(Utils.getOwnerRemoteId())) {
      searchScope = SearchScope.USER;
      return searchScope;
    } else {
      searchScope = SearchScope.NONE;
      return searchScope;
    }
  }

  private boolean canModifyChartSettings() {
    if (canModifyChartSettings != null) {
      return canModifyChartSettings;
    }
    ConversationState current = ConversationState.getCurrent();
    if (current == null || current.getIdentity() == null) {
      canModifyChartSettings = false;
      return canModifyChartSettings;
    }
    Identity identity = current.getIdentity();
    String userId = identity.getUserId();
    if (StringUtils.isBlank(userId) || StringUtils.equals(userId, IdentityConstants.ANONIM)
        || StringUtils.equals(userId, IdentityConstants.SYSTEM)) {
      canModifyChartSettings = false;
      return canModifyChartSettings;
    }

    String groupId = getAnalyticsService().getAdministratorsPermission();
    if (StringUtils.isNotBlank(groupId) && identity.isMemberOf(MembershipEntry.parse(groupId))) {
      canModifyChartSettings = true;
      return canModifyChartSettings;
    }

    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      canModifyChartSettings = getSpaceService().isSuperManager(userId) || getSpaceService().isManager(space, userId);
      return canModifyChartSettings;
    }

    canModifyChartSettings = false;
    return canModifyChartSettings;
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
