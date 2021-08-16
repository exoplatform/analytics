package org.exoplatform.analytics.api.websocket;

import org.apache.commons.lang3.StringUtils;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

public class AnalyticsWebSocketService {

  private static final String   EXO_ANALYTICS_MESSAGE_EVENT = "exo.analytics.websocket.messageReceived";

  private static final Log      LOG                         = ExoLogger.getLogger(AnalyticsWebSocketService.class);

  public static final String    COMETD_CHANNEL              = "/service/analytics";

  private ListenerService       listenerService;

  private ContinuationService   continuationService;

  private EXoContinuationBayeux continuationBayeux;

  private ServerChannel         channel;

  private String                cometdContextName;

  public void init() {
    if (channel == null) {
      channel = getContinuationBayeux().createChannelIfAbsent(COMETD_CHANNEL).getReference();
      channel.addListener(new ServerChannel.MessageListener() {
        @Override
        public boolean onMessage(ServerSession from, ServerChannel channel, Mutable message) {
          try {
            if (message == null || message.getData() == null) {
              LOG.warn("Empty analytics WebSocket message is received");
              return false;
            }
            if (from == null) {
              LOG.warn("Empty analytics WebSocket session is received");
              return false;
            }
            if (channel == null || !StringUtils.equals(channel.getId(), COMETD_CHANNEL)) {
              LOG.warn("Empty WebSocket channel received");
              return false;
            }
            if (!StringUtils.equals(channel.getId(), COMETD_CHANNEL)) {
              LOG.debug("Not Analytics WebSocket channel");
              return false;
            }

            Object data = message.getData();
            AnalyticsWebSocketMessage wsMessage = AnalyticsUtils.fromJsonString(data.toString(), AnalyticsWebSocketMessage.class);
            if (StringUtils.isBlank(wsMessage.getUserName())) {
              LOG.warn("Empty WebSocket username received");
              return false;
            }
            if (StringUtils.isBlank(wsMessage.getToken())) {
              LOG.warn("Empty WebSocket user token received");
              return false;
            }
            String userToken = getUserToken(wsMessage.getUserName());
            if (!StringUtils.equals(userToken, wsMessage.getToken())) {
              LOG.warn("Wrong WebSocket token received for user {}", wsMessage.getUserName());
              return false;
            }

            getListenerService().broadcast(EXO_ANALYTICS_MESSAGE_EVENT, this, wsMessage);
            return true;
          } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
              LOG.warn("Error when parsing analytics ws message", e);
            } else {
              LOG.warn("Error when parsing analytics ws message: {}", e.getMessage());
            }
            return false;
          }
        }
      });
    }
  }

  public UserSettings getUserSettings(String username) {
    UserSettings userSettings = new UserSettings();
    userSettings.setCometdToken(getUserToken(username));
    userSettings.setCometdContext(getCometdContextName());
    return userSettings;
  }

  private String getUserToken(String username) {
    try {
      return getContinuationService().getUserToken(username);
    } catch (Exception e) {
      LOG.warn("Could not retrieve continuation token for user " + username, e);
      return "";
    }
  }

  private ListenerService getListenerService() {
    if (listenerService == null) {
      listenerService = CommonsUtils.getService(ListenerService.class);
    }
    return listenerService;
  }

  private String getCometdContextName() {
    if (cometdContextName == null) {
      cometdContextName = (getContinuationBayeux() == null ? "cometd" : getContinuationBayeux().getCometdContextName());
    }
    return cometdContextName;
  }

  private EXoContinuationBayeux getContinuationBayeux() {
    if (continuationBayeux == null) {
      continuationBayeux = CommonsUtils.getService(EXoContinuationBayeux.class);
    }
    return continuationBayeux;
  }

  private ContinuationService getContinuationService() {
    if (continuationService == null) {
      continuationService = CommonsUtils.getService(ContinuationService.class);
    }
    return continuationService;
  }

}
