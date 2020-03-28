package org.exoplatform.analytics.listener;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addStatisticData;
import static org.exoplatform.analytics.utils.AnalyticsUtils.getUserIdentityId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticData.StatisticStatus;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.web.application.*;
import org.exoplatform.webui.application.WebuiRequestContext;

public class PageAccessListener extends BaseComponentPlugin implements ApplicationLifecycle<WebuiRequestContext> {

  private static final Log  LOG                = ExoLogger.getLogger(PageAccessListener.class);

  private ThreadLocal<Long> operationStartTime = new ThreadLocal<>();

  @Override
  public void onInit(Application app) throws Exception {
    // Not used
  }

  @Override
  public void onStartRequest(Application app, WebuiRequestContext context) throws Exception {
    operationStartTime.set(System.currentTimeMillis());
  }

  @Override
  public void onEndRequest(Application app, WebuiRequestContext context) throws Exception {
    StatisticData statisticData = buildStatisticData(context);
    if (statisticData != null) {
      addStatisticData(statisticData);
    }
  }

  @Override
  public void onFailRequest(Application app, WebuiRequestContext context, RequestFailure failureType) {
    // Not used
  }

  @Override
  public void onDestroy(Application app) throws Exception {
    // Not used
  }

  private StatisticData buildStatisticData(WebuiRequestContext context) {
    try {
      PortalRequestContext portalRequestContext = (PortalRequestContext) context;
      if (portalRequestContext == null) {
        return null;
      }

      StatisticData statisticData = new StatisticData();
      statisticData.setModule("portal");
      if (portalRequestContext.useAjax()) {
        statisticData.setSubModule("webui");
        statisticData.setOperation("ajaxRequest");
      } else {
        statisticData.setSubModule("page");
        statisticData.setOperation("pageDisplay");
      }
      long userIdentityId = getUserIdentityId(context.getRemoteUser());
      statisticData.setUserId(userIdentityId);

      Space space = SpaceUtils.getSpaceByContext();
      if (space != null) {
        statisticData.setSpaceId(Long.parseLong(space.getId()));
        statisticData.addParameter("spaceTemplate", space.getTemplate());
      }

      HttpServletRequest httpRequest = portalRequestContext.getRequest();
      if (httpRequest != null) {
        statisticData.addParameter("httpRequestMethod", httpRequest.getMethod());
        statisticData.addParameter("httpRequestUri", httpRequest.getRequestURI());
        statisticData.addParameter("httpRequestProtocol", httpRequest.getProtocol());
        statisticData.addParameter("httpRequestContentType", httpRequest.getContentType());
        statisticData.addParameter("httpRequestContentLength", httpRequest.getContentLength());
      }

      statisticData.addParameter("userLocale", portalRequestContext.getLocale());
      statisticData.addParameter("portalOwner", portalRequestContext.getPortalOwner());
      statisticData.addParameter("portalUri", portalRequestContext.getPortalURI());

      UIPortal uiportal = Util.getUIPortal();
      if (uiportal != null) {
        UserNode node = uiportal.getSelectedUserNode();
        if (node != null) {
          statisticData.addParameter("pageUri", node.getURI());
          statisticData.addParameter("pageName", node.getName());
        }
      }

      HttpServletResponse httpResponse = portalRequestContext.getResponse();
      if (httpResponse != null) {
        statisticData.addParameter("httpResponseContentType", httpResponse.getContentType());
        statisticData.addParameter("httpResponseContentLength", httpResponse.getBufferSize());
        statisticData.addParameter("httpResponseStatus", httpResponse.getStatus());

        if (httpResponse.getStatus() >= 400) {
          statisticData.setErrorCode(httpResponse.getStatus());
          statisticData.setStatus(StatisticStatus.KO);
        } else {
          statisticData.setStatus(StatisticStatus.OK);
        }
      }

      statisticData.setDuration(getDuration());
      return statisticData;
    } catch (Exception e) {
      LOG.debug("Error computing page statistics", e);
      return null;
    }
  }

  private long getDuration() {
    Long startTime = operationStartTime.get();
    if (startTime == null) {
      return 0;
    }
    operationStartTime.remove();
    return System.currentTimeMillis() - startTime;
  }

}
