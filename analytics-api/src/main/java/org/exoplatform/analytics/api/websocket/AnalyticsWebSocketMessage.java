package org.exoplatform.analytics.api.websocket;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.analytics.api.service.StatisticWatcher;

import lombok.Data;

@Data
public class AnalyticsWebSocketMessage {

  /**
   * Watcher name from {@link StatisticWatcher#getName()}
   */
  String              name;

  /**
   * User login name
   */
  String              userName;

  /**
   * Current technical space id
   */
  String              spaceId;

  /**
   * Current portalUri
   */
  String              portalUri;

  /**
   * User cometd Token
   */
  String              token;

  /**
   * Collected DOM properties switch {@link StatisticWatcher#getDomProperties()}
   * and DOM event properties collected using
   * {@link StatisticWatcher#getDomEventProperties()}
   */
  Map<String, String> parameters = new HashMap<>();

}
