package org.exoplatform.analytics.listener.websocket;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.api.service.StatisticWatcher;
import org.exoplatform.analytics.api.websocket.AnalyticsWebSocketMessage;
import org.exoplatform.analytics.api.websocket.AnalyticsWebSocketService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Asynchronous
public class WebSocketUIStatisticListener extends Listener<AnalyticsWebSocketService, AnalyticsWebSocketMessage> {
  private static final Log LOG = ExoLogger.getLogger(WebSocketUIStatisticListener.class);

  private AnalyticsService analyticsService;

  public WebSocketUIStatisticListener(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @Override
  public void onEvent(Event<AnalyticsWebSocketService, AnalyticsWebSocketMessage> event) throws Exception {
    AnalyticsWebSocketMessage message = event.getData();
    long userId = getUserIdentityId(message.getUserName());
    if (userId <= 0) {
      LOG.debug("User not found in state, userId= {} ", userId);
      return;
    }

    String spaceId = message.getSpaceId();
    if (StringUtils.isBlank(spaceId) && StringUtils.isNotBlank(message.getSpacePrettyName())) {
      long spaceIdLong = getSpaceId(message.getSpacePrettyName());
      if (spaceIdLong > 0) {
        spaceId = String.valueOf(spaceIdLong);
      }
    }

    String module = null;
    String subModule = null;
    String operation = null;
    Map<String, String> data = message.getParameters();
    if (data == null) {
      data = new HashMap<>();
    }

    if (StringUtils.isNotBlank(message.getName())) {
      StatisticWatcher uiWatcher = analyticsService.getUIWatcher(message.getName());
      if (uiWatcher == null) {
        LOG.warn("Can't find watcher with name '{}'", message.getName());
        return;
      }

      module = uiWatcher.getModule() == null ? uiWatcher.getModule() : "portal";
      subModule = uiWatcher.getSubModule() == null ? uiWatcher.getSubModule() : "ui";
      operation = uiWatcher.getOperation();

      if (uiWatcher.getParameters() != null && !uiWatcher.getParameters().isEmpty()) {
        data.putAll(uiWatcher.getParameters());
      }
    }

    if (StringUtils.isNotBlank(message.getModule())) {
      module = message.getModule();
    }

    if (StringUtils.isNotBlank(message.getSubModule())) {
      subModule = message.getSubModule();
    }

    if (StringUtils.isNotBlank(message.getOperation())) {
      operation = message.getOperation();
    }

    StatisticData statisticData = new StatisticData();
    statisticData.setModule(module);
    statisticData.setSubModule(subModule);
    statisticData.setOperation(operation);
    statisticData.setUserId(userId);
    if (StringUtils.isNotBlank(spaceId)) {
      statisticData.setSpaceId(Long.parseLong(spaceId));
    }
    if (StringUtils.isNotBlank(message.getPortalUri())) {
      data.put("portalUri", message.getPortalUri());
    }
    if (StringUtils.isNotBlank(message.getName())) {
      data.put("watcher", message.getName());
    }
    statisticData.setParameters(data);
    addStatisticData(statisticData);
  }

}
