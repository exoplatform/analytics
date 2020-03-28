package org.exoplatform.analytics.api.websocket;

import lombok.Data;

@Data
public class UserSettings {

  private String cometdChannel = AnalyticsWebSocketService.COMETD_CHANNEL;

  private String cometdToken;

  private String cometdContext;

}
