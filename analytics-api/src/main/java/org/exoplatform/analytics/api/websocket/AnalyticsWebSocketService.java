package org.exoplatform.analytics.api.websocket;

import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

public class AnalyticsWebSocketService {

  private static final Log      LOG            = ExoLogger.getLogger(AnalyticsWebSocketService.class);

  public static final String    COMETD_CHANNEL = "/service/analytics";

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
          Object data = message.getData();
          System.out.println("received data" + data);
          return true;
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

  protected String getUserToken(String username) {
    try {
      return getContinuationService().getUserToken(username);
    } catch (Exception e) {
      LOG.warn("Could not retrieve continuation token for user " + username, e);
      return "";
    }
  }

  protected String getCometdContextName() {
    if (cometdContextName == null) {
      getContinuationBayeux();
      cometdContextName = (continuationBayeux == null ? "cometd" : continuationBayeux.getCometdContextName());
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
