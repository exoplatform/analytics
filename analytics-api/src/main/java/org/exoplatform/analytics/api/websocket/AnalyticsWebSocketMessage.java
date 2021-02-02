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
  private String              name;

  /**
   * statistic module
   */
  private String              module;

  /**
   * statistic subModule
   */
  private String              subModule;

  /**
   * statistic operation
   */
  private String              operation;

  /**
   * User login name
   */
  private String              userName;

  /**
   * Current technical space pretty name
   */
  private String              spacePrettyName;

  /**
   * Current technical space id
   */
  private String              spaceId;

  /**
   * Current portalUri
   */
  private String              portalUri;

  /**
   * User cometd Token
   */
  private String              token;

  /**
   * Collected DOM properties switch {@link StatisticWatcher#getDomProperties()}
   * and DOM event properties collected using
   * {@link StatisticWatcher#getDomEventProperties()}
   */
  private Map<String, String> parameters = new HashMap<>();

}
