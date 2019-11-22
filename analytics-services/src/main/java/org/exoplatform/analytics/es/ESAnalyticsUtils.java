package org.exoplatform.analytics.es;

import org.apache.commons.lang3.StringUtils;

public class ESAnalyticsUtils {

  public static final String   ES_INDEX_PLACEHOLDER                        = "@ES_INDEX_PLACEHOLDER@";

  public static final String   ES_TYPE                                     = "analytics";

  private static final String  ES_INDEX_CLIENT_PROPERTY_NAME               = "exo.es.index.server.url";

  private static final String  ES_INDEX_CLIENT_PROPERTY_USERNAME           = "exo.es.index.server.username";

  private static final String  ES_INDEX_CLIENT_PROPERTY_PWD                = "exo.es.index.server.password";                  // NOSONAR

  private static final String  ES_ANALYTICS_INDEX_CLIENT_PROPERTY_NAME     = "exo.es.analytics.index.server.url";

  private static final String  ES_ANALYTICS_INDEX_PER_DAY_PROPERTY_NAME    = "exo.es.analytics.index.per.day";

  private static final String  ES_ANALYTICS_INDEX_CLIENT_PROPERTY_USERNAME = "exo.es.analytics.index.server.username";

  private static final String  ES_ANALYTICS_INDEX_CLIENT_PROPERTY_PWD      = "exo.es.analytics.index.server.password";        // NOSONAR

  private static final String  ES_INDEX_CLIENT_DEFAULT                     = "http://127.0.0.1:9200";

  private static final long    DAY_IN_MS                                   = 86400000L;

  private static String        serverURL                                   = null;

  private static String        serverUsername                              = null;

  private static String        serverPassword                              = null;                                            // NOSONAR

  private static final boolean IS_EMBEDDED                                 =
                                           StringUtils.isBlank(System.getProperty(ES_ANALYTICS_INDEX_CLIENT_PROPERTY_NAME));

  private static final boolean INDEX_PER_DAY                               =
                                             Boolean.parseBoolean(System.getProperty(ES_ANALYTICS_INDEX_PER_DAY_PROPERTY_NAME,
                                                                                     "false"));

  private ESAnalyticsUtils() {
    // Static class
  }

  public static final String getIndex(long timestamp) {
    if (INDEX_PER_DAY) {
      long indexSuffix = timestamp / DAY_IN_MS;
      return "analytics_" + indexSuffix;
    } else {
      return "analytics";
    }
  }

  protected static final String getESServerURL() {
    if (serverURL == null) {
      if (IS_EMBEDDED) {
        serverURL = System.getProperty(ES_INDEX_CLIENT_PROPERTY_NAME);
      } else {
        serverURL = System.getProperty(ES_ANALYTICS_INDEX_CLIENT_PROPERTY_NAME);
      }
      if (StringUtils.isBlank(serverURL)) {
        serverURL = ES_INDEX_CLIENT_DEFAULT;
      }
    }
    return serverURL;
  }

  protected static final String getESServerUsername() {
    if (serverUsername == null) {
      if (IS_EMBEDDED) {
        serverUsername = System.getProperty(ES_INDEX_CLIENT_PROPERTY_USERNAME);
      } else {
        serverUsername = System.getProperty(ES_ANALYTICS_INDEX_CLIENT_PROPERTY_USERNAME);
      }
    }
    return serverUsername;
  }

  protected static final String getESServerPassword() {
    if (serverPassword == null) {
      if (IS_EMBEDDED) {
        serverPassword = System.getProperty(ES_INDEX_CLIENT_PROPERTY_PWD);
      } else {
        serverPassword = System.getProperty(ES_ANALYTICS_INDEX_CLIENT_PROPERTY_PWD);
      }
    }
    return serverPassword;
  }

}
