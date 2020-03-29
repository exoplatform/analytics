package org.exoplatform.analytics.listener;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addStatisticData;
import static org.exoplatform.analytics.utils.AnalyticsUtils.getUserIdentityId;

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

    StatisticWatcher uiWatcher = analyticsService.getUIWatcher(message.getName());
    if (uiWatcher == null) {
      LOG.warn("Can't find watcher with name '{}'", message.getName());
      return;
    }

    StatisticData statisticData = new StatisticData();
    statisticData.setModule("portal");
    statisticData.setSubModule("ui");
    statisticData.setOperation(uiWatcher.getOperation());
    statisticData.setUserId(userId);
    if (StringUtils.isNotBlank(message.getSpaceId())) {
      statisticData.setSpaceId(Long.parseLong(message.getSpaceId()));
    }
    Map<String, String> data = message.getParameters();
    if (data == null) {
      data = new HashMap<>();
    }
    data.put("portalUri", message.getPortalUri());
    data.put("watcher", message.getName());
    if (uiWatcher.getParameters() != null && !uiWatcher.getParameters().isEmpty()) {
      data.putAll(uiWatcher.getParameters());
    }
    statisticData.setParameters(data);

    addStatisticData(statisticData);
  }

}
