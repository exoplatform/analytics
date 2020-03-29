package org.exoplatform.analytics.api.service;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class StatisticWatcher {

  /**
   * Statistic data: name field
   */
  private String              name;

  /**
   * Statistic data: operation field
   */
  private String              operation;

  /**
   * Statistic data: additional embedded parameters
   */
  private Map<String, String> parameters;

  /**
   * DOM jquery selector, used to search elements
   */
  private String              domSelector;

  /**
   * DOM jquery event to listen, used to trigger storing new statistic data
   */
  private String              domEvent;

  /**
   * DOM jquery element attributes ('attr' method) or data ('data' method) to
   * collect and store in statistic data
   */
  private List<String>        domProperties;

  /**
   * DOM triggered event attributes to collect and store in statistic data
   */
  private List<String>        domEventProperties;

}
