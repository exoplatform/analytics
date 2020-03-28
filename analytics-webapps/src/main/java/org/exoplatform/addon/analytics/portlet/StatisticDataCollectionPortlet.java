package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.List;

import javax.portlet.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.api.service.StatisticWatcher;
import org.exoplatform.analytics.api.websocket.AnalyticsWebSocketService;
import org.exoplatform.analytics.api.websocket.UserSettings;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.ExoContainerContext;

public class StatisticDataCollectionPortlet extends GenericPortlet {

  private static final String       JSP_FILE = "/WEB-INF/jsp/statistics-collection.jsp";

  private AnalyticsWebSocketService analyticsWebSocketService;

  private AnalyticsService          analyticsService;

  @Override
  public void init() throws PortletException {
    super.init();
    getAnalyticsWebSocketService().init();
  }

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    if (StringUtils.isNotBlank(request.getRemoteUser())) {
      UserSettings userSettings = getAnalyticsWebSocketService().getUserSettings(request.getRemoteUser());
      request.setAttribute("userSettings", AnalyticsUtils.toJsonString(userSettings));

      List<StatisticWatcher> uiWatchers = getAnalyticsService().getUIWatchers();
      request.setAttribute("uiWatchers", AnalyticsUtils.toJsonString(uiWatchers));

      PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(JSP_FILE);
      dispatcher.include(request, response);
    }
  }

  public AnalyticsService getAnalyticsService() {
    if (analyticsService == null) {
      analyticsService = ExoContainerContext.getService(AnalyticsService.class);
    }
    return analyticsService;
  }

  public AnalyticsWebSocketService getAnalyticsWebSocketService() {
    if (analyticsWebSocketService == null) {
      analyticsWebSocketService = ExoContainerContext.getService(AnalyticsWebSocketService.class);
    }
    return analyticsWebSocketService;
  }
}
