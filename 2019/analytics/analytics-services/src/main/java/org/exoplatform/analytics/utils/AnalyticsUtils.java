package org.exoplatform.analytics.utils;

import static java.time.temporal.ChronoField.*;

import java.io.ByteArrayInputStream;
import java.time.*;
import java.time.format.*;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import org.exoplatform.analytics.api.service.AnalyticsQueueService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.ws.frameworks.json.JsonGenerator;
import org.exoplatform.ws.frameworks.json.JsonParser;
import org.exoplatform.ws.frameworks.json.impl.*;

public class AnalyticsUtils {

  public static final String            FIELD_ERROR_MESSAGE              = "errorMessage";

  public static final String            FIELD_ERROR_CODE                 = "errorCode";

  public static final String            FIELD_STATUS                     = "status";

  public static final String            FIELD_OPERATION                  = "operation";

  public static final String            FIELD_SUB_MODULE                 = "subModule";

  public static final String            FIELD_MODULE                     = "module";

  public static final String            FIELD_SPACE_ID                   = "spaceId";

  public static final String            FIELD_USER_ID                    = "userId";

  public static final String            FIELD_HOUR                       = "hour";

  public static final String            FIELD_DAY_OF_YEAR                = "dayOfYear";

  public static final String            FIELD_DAY_OF_WEEK                = "dayOfWeek";

  public static final String            FIELD_DAY_OF_MONTH               = "dayOfMonth";

  public static final String            FIELD_WEEK                       = "week";

  public static final String            FIELD_MONTH                      = "month";

  public static final String            FIELD_YEAR                       = "year";

  public static final String            FIELD_TIMESTAMP                  = "timestamp";

  public static final String            FIELD_MODIFIER_USER_SOCIAL_ID    = "modifierSocialId";

  public static final List<String>      DEFAULT_FIELDS                   = Arrays.asList(FIELD_ERROR_MESSAGE,
                                                                                         FIELD_ERROR_CODE,
                                                                                         FIELD_STATUS,
                                                                                         FIELD_OPERATION,
                                                                                         FIELD_MODULE,
                                                                                         FIELD_SUB_MODULE,
                                                                                         FIELD_SPACE_ID,
                                                                                         FIELD_USER_ID,
                                                                                         FIELD_HOUR,
                                                                                         FIELD_DAY_OF_YEAR,
                                                                                         FIELD_DAY_OF_YEAR,
                                                                                         FIELD_DAY_OF_WEEK,
                                                                                         FIELD_DAY_OF_MONTH,
                                                                                         FIELD_WEEK,
                                                                                         FIELD_MONTH,
                                                                                         FIELD_YEAR,
                                                                                         FIELD_TIMESTAMP);

  public static final String            ANALYTICS_NEW_DATA_EVENT         = "exo.addon.analytics.data.new";

  public static final String            ES_ANALYTICS_PROCESSOR_ID        = "exo.addon.analytics.processor.es";

  public static final JsonParser        JSON_PARSER                      = new JsonParserImpl();

  public static final JsonGenerator     JSON_GENERATOR                   = new JsonGeneratorImpl();

  private static final Pattern          JSON_CLEANER_REPLACEMENT_PATTERN = Pattern.compile("([\\]}]+),([\\]}]+)");

  public static final DateTimeFormatter YEAR_WEEK                        = new DateTimeFormatterBuilder()
                                                                                                         .parseCaseInsensitive()
                                                                                                         .appendValue(IsoFields.WEEK_BASED_YEAR,
                                                                                                                      4,
                                                                                                                      10,
                                                                                                                      SignStyle.EXCEEDS_PAD)
                                                                                                         .appendLiteral("-W")
                                                                                                         .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR,
                                                                                                                      2)
                                                                                                         .optionalStart()
                                                                                                         .appendOffsetId()
                                                                                                         .toFormatter();

  public static final DateTimeFormatter YEAR_MONTH                       = new DateTimeFormatterBuilder()
                                                                                                         .appendValue(YEAR,
                                                                                                                      4,
                                                                                                                      10,
                                                                                                                      SignStyle.EXCEEDS_PAD)
                                                                                                         .appendLiteral('-')
                                                                                                         .appendValue(MONTH_OF_YEAR,
                                                                                                                      2)
                                                                                                         .toFormatter();

  public static final DateTimeFormatter YEAR_MONTH_DATE_HOUR             = new DateTimeFormatterBuilder()
                                                                                                         .appendValue(YEAR,
                                                                                                                      4,
                                                                                                                      10,
                                                                                                                      SignStyle.EXCEEDS_PAD)
                                                                                                         .appendLiteral('-')
                                                                                                         .appendValue(MONTH_OF_YEAR,
                                                                                                                      2)
                                                                                                         .appendLiteral('-')
                                                                                                         .appendValue(DAY_OF_MONTH,
                                                                                                                      2)
                                                                                                         .appendLiteral('T')
                                                                                                         .appendValue(HOUR_OF_DAY,
                                                                                                                      2)
                                                                                                         .toFormatter();

  private AnalyticsUtils() {
  }

  public static final String getYearMonthDayHour(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearMonthDayHour(date);
  }

  public static final String getYearMonthDayHour(LocalDateTime date) {
    return YEAR_MONTH_DATE_HOUR.format(date);
  }

  public static final String getYearMonthDay(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearMonthDay(date);
  }

  public static final String getYearMonthDay(LocalDateTime date) {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }

  public static final String getYearMonth(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearMonth(date);
  }

  public static final String getYearMonth(LocalDateTime date) {
    return YEAR_MONTH.format(date);
  }

  public static final String getYearWeek(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearWeek(date);
  }

  public static final String getYearWeek(LocalDateTime date) {
    return YEAR_WEEK.format(date);
  }

  public static final String toJsonString(Object object) {
    try {
      return JSON_GENERATOR.createJsonObject(object).toString();
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing object to string " + object, e);
    }
  }

  public static final <T> T fromJsonString(String value, Class<T> resultClass) {
    try {
      if (StringUtils.isBlank(value)) {
        return null;
      }
      JsonDefaultHandler jsonDefaultHandler = new JsonDefaultHandler();
      JSON_PARSER.parse(new ByteArrayInputStream(value.getBytes()), jsonDefaultHandler);
      return ObjectBuilder.createObject(resultClass, jsonDefaultHandler.getJsonObject());
    } catch (JsonException e) {
      throw new IllegalStateException("Error creating object from string : " + value, e);
    }
  }

  public static long timeToSeconds(LocalDateTime time) {
    return time.atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
  }

  public static final String fixJSONStringFormat(String jsonString) {
    do {
      jsonString = jsonString.replaceAll(" ", "")
                             .replaceAll("\n", "")
                             .replaceAll(",+", ",")
                             .replaceAll("([\\]}]+),([\\]}]+)", "$1$2");
    } while (JSON_CLEANER_REPLACEMENT_PATTERN.matcher(jsonString).find());
    return jsonString;
  }

  public static final String collectionToJSONString(Collection<String> collection) {
    return new JSONArray(collection).toString();
  }

  public static final void addStatisticData(StatisticData statisticData) {
    AnalyticsQueueService analyticsQueueService = CommonsUtils.getService(AnalyticsQueueService.class);
    analyticsQueueService.put(statisticData);
  }

  public static long getUserIdentityId(String username) {
    return getIdentityId(OrganizationIdentityProvider.NAME, username);
  }

  public static long getIdentityId(String providerId, String remoteId) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
    return identity == null ? 0 : Long.parseLong(identity.getId());
  }

  public static long getUserIdentityId(ConversationState currentState) {
    String username = getUsername(currentState);
    boolean unkownUser = isUnkownUser(username);
    if (unkownUser) {
      return 0;
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
    return identity == null ? 0 : Long.parseLong(identity.getId());
  }

  public static long getCurrentUserIdentityId() {
    ConversationState currentState = ConversationState.getCurrent();
    return getUserIdentityId(currentState);
  }

  public static boolean isUnkownUser(String username) {
    return username == null || StringUtils.equals(username, IdentityConstants.ANONIM)
        || StringUtils.equals(username, IdentityConstants.SYSTEM);
  }

  public static String getUsername(ConversationState currentState) {
    return currentState == null || currentState.getIdentity() == null
        || currentState.getIdentity().getUserId() == null ? null : currentState.getIdentity().getUserId();
  }
}
