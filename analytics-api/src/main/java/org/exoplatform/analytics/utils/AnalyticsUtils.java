package org.exoplatform.analytics.utils;

import static java.time.temporal.ChronoField.*;

import java.io.ByteArrayInputStream;
import java.time.*;
import java.time.format.*;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.*;

import org.exoplatform.analytics.api.service.StatisticDataQueueService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticData.StatisticStatus;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.ws.frameworks.json.impl.*;

public class AnalyticsUtils {
  private static final Log              LOG                              = ExoLogger.getLogger(AnalyticsUtils.class);

  public static final short             MAX_BULK_DOCUMENTS               = 100;

  public static final String            VALUES_SEPARATOR                 = ",";

  public static final String            FIELD_IS_ANALYTICS               = "isAnalytics";

  public static final String            FIELD_ERROR_MESSAGE              = "errorMessage";

  public static final String            FIELD_ERROR_CODE                 = "errorCode";

  public static final String            FIELD_STATUS                     = "status";

  public static final String            FIELD_OPERATION                  = "operation";

  public static final String            FIELD_SUB_MODULE                 = "subModule";

  public static final String            FIELD_MODULE                     = "module";

  public static final String            FIELD_SPACE_ID                   = "spaceId";

  public static final String            FIELD_DURATION                   = "duration";

  public static final String            FIELD_USER_ID                    = "userId";

  public static final String            FIELD_TIMESTAMP                  = "timestamp";

  public static final String            FIELD_MODIFIER_USER_SOCIAL_ID    = "modifierSocialId";

  public static final String            FIELD_SOCIAL_IDENTITY_ID         = "identityId";

  public static final String            AVATAR                           = "avatar";

  public static final String            IMAGE_SIZE                       = "imageSize";

  public static final String            IMAGE_TYPE                       = "imageType";

  public static final List<String>      COMPUTED_CHART_LABEL             = Arrays.asList(FIELD_MODIFIER_USER_SOCIAL_ID,                                 // NOSONAR
                                                                                         FIELD_SOCIAL_IDENTITY_ID,
                                                                                         FIELD_USER_ID,
                                                                                         FIELD_SPACE_ID);

  public static final DateTimeFormatter DATE_FORMATTER                   = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");

  public static final List<String>      DEFAULT_FIELDS                   = Arrays.asList(FIELD_IS_ANALYTICS,                                            // NOSONAR
                                                                                         FIELD_ERROR_MESSAGE,
                                                                                         FIELD_ERROR_CODE,
                                                                                         FIELD_STATUS,
                                                                                         FIELD_OPERATION,
                                                                                         FIELD_MODULE,
                                                                                         FIELD_SUB_MODULE,
                                                                                         FIELD_SPACE_ID,
                                                                                         FIELD_USER_ID,
                                                                                         FIELD_TIMESTAMP);

  public static final String            ES_ANALYTICS_PROCESSOR_ID        = "exo.addon.analytics.processor.es";

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
      if (object instanceof Collection) {
        return new JsonGeneratorImpl().createJsonArray((Collection<?>) object).toString();
      } else {
        return new JsonGeneratorImpl().createJsonObject(object).toString();
      }
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing object to string " + object, e);
    }
  }

  public static final String compueLabel(String chartKey, String chartValue) {
    String defaultLabel = (chartKey == null ? "" : chartKey.replace(".keyword", "") + "=") + chartValue;
    if (StringUtils.isBlank(chartKey) || StringUtils.isBlank(chartValue) || !COMPUTED_CHART_LABEL.contains(chartKey)) {
      return defaultLabel;
    }

    if (StringUtils.equals(chartKey, FIELD_SPACE_ID)) {
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceById(chartValue);
      if (space == null) {
        return defaultLabel;
      } else {
        return space.getDisplayName();
      }
    } else {
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      if (identityManager == null) {
        return defaultLabel;
      } else {
        Identity identity = identityManager.getIdentity(chartValue);
        if (identity == null) {
          return defaultLabel;
        } else {
          return identity.getProfile().getFullName();
        }
      }
    }
  }

  public static final <T> T fromJsonString(String value, Class<T> resultClass) {
    try {
      if (StringUtils.isBlank(value)) {
        return null;
      }
      JsonDefaultHandler jsonDefaultHandler = new JsonDefaultHandler();
      new JsonParserImpl().parse(new ByteArrayInputStream(value.getBytes()), jsonDefaultHandler);
      return ObjectBuilder.createObject(resultClass, jsonDefaultHandler.getJsonObject());
    } catch (JsonException e) {
      throw new IllegalStateException("Error creating object from string : " + value, e);
    }
  }

  public static long timeToMilliseconds(LocalDateTime time) {
    return time.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
  }

  public static final String fixJSONStringFormat(String jsonString) {
    do {
      jsonString = jsonString.replace(" ", "")
                             .replace("\n", "")
                             .replaceAll(",+", VALUES_SEPARATOR)
                             .replaceAll("([\\]}]+),([\\]}]+)", "$1$2");
    } while (JSON_CLEANER_REPLACEMENT_PATTERN.matcher(jsonString).find());
    return jsonString;
  }

  public static final String collectionToJSONString(String value) {
    String[] valuesString = value.split(VALUES_SEPARATOR);
    List<String> valuesList = new ArrayList<>();
    Collections.addAll(valuesList, valuesString);
    return new JSONArray(valuesList.stream().map(String::trim).collect(Collectors.toList())).toString();
  }

  public static final JSONObject getJSONObject(JSONObject jsonObject, int i, String... keys) {
    if (keys == null || i >= keys.length) {
      return null;
    }
    if (jsonObject.has(keys[i])) {
      try {
        jsonObject = jsonObject.getJSONObject(keys[i]);
        i++;
        if (i == keys.length) {
          return jsonObject;
        } else {
          return getJSONObject(jsonObject, i, keys);
        }
      } catch (JSONException e) {
        LOG.warn("Error getting key object with {}", keys[i], e);
        return null;
      }
    }
    return null;
  }

  @ExoTransactional
  public static final void addStatisticData(StatisticData statisticData) {
    if (statisticData == null) {
      return;
    }
    if (statisticData.getTimestamp() <= 0) {
      statisticData.setTimestamp(System.currentTimeMillis());
    }
    if (statisticData.getStatus() == null) {
      statisticData.setStatus(StatisticStatus.OK);
    }

    try {
      StatisticDataQueueService analyticsQueueService = CommonsUtils.getService(StatisticDataQueueService.class);
      analyticsQueueService.put(statisticData);
    } catch (Exception e) {
      LOG.warn("Error adding analytics Queue entry: {}", statisticData, e);
    }
  }

  public static long getUserIdentityId(String username) {
    return getIdentityId(OrganizationIdentityProvider.NAME, username);
  }

  public static Space getSpaceByPrettyName(String prettyName) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    return spaceService.getSpaceByPrettyName(prettyName);
  }

  public static Space getSpaceById(String spaceId) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    return spaceService.getSpaceById(spaceId);
  }

  public static Identity getIdentity(String identityId) {
    if (StringUtils.isBlank(identityId)) {
      return null;
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getIdentity(identityId);
  }

  public static long getIdentityId(String identityId) {
    Identity identity = getIdentity(identityId);
    return identity == null ? 0 : Long.parseLong(identity.getId());
  }

  @ExoTransactional
  public static Identity getIdentity(String providerId, String remoteId) {
    if (StringUtils.isBlank(remoteId)) {
      return null;
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getOrCreateIdentity(providerId, remoteId);
  }

  public static long getIdentityId(String providerId, String remoteId) {
    Identity identity = getIdentity(providerId, remoteId);
    return identity == null ? 0 : Long.parseLong(identity.getId());
  }

  @ExoTransactional
  public static long getUserIdentityId(ConversationState currentState) {
    String username = getUsername(currentState);
    boolean unkownUser = isUnkownUser(username);
    if (unkownUser) {
      return 0;
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
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

  public static String formatDate(long timeInMilliseconds) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(String.valueOf(timeInMilliseconds))),
                                                     TimeZone.getDefault().toZoneId());
    return dateTime.format(DATE_FORMATTER);
  }

  public static void addSpaceStatistics(StatisticData statisticData, Space space) {
    if (space == null) {
      return;
    }
    statisticData.setSpaceId(Long.parseLong(space.getId()));
    statisticData.addParameter("spaceTemplate", space.getTemplate());
    statisticData.addParameter("spaceVisibility", space.getVisibility());
    statisticData.addParameter("spaceRegistration", space.getRegistration());
    statisticData.addParameter("spaceCreatedTime", space.getCreatedTime());
    statisticData.addParameter("spaceMembersCount", getSize(space.getMembers()));
    statisticData.addParameter("spaceManagersCount", getSize(space.getManagers()));
    statisticData.addParameter("spaceRedactorsCount", getSize(space.getRedactors()));
    statisticData.addParameter("spaceInviteesCount", getSize(space.getInvitedUsers()));
    statisticData.addParameter("spacePendingCount", getSize(space.getPendingUsers()));
  }

  private static int getSize(String[] array) {
    return array == null ? 0 : new HashSet<>(Arrays.asList(array)).size();
  }
}
