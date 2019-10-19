package org.exoplatform.analytics.es.injection;

import static org.exoplatform.analytics.utils.AnalyticsUtils.FIELD_MODIFIER_USER_SOCIAL_ID;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.LongStream;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsDataGenerator {
  private static final Log          LOG                                             =
                                        ExoLogger.getLogger(AnalyticsDataGenerator.class);

  public static final String        ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH     = "/tmp/analytics-generated-data";           // NOSONAR

  private static final String       ANALYTICS_GENERATED_SPACE_DATA_FILE_PATH        =
                                                                             ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH
                                                                                 + "/analytics-space-data.json";                 // NOSONAR

  private static final String       ANALYTICS_GENERATED_LOGIN_DATA_FILE_PATH        =
                                                                             ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH
                                                                                 + "/analytics-login-data.json";                 // NOSONAR

  private static final String       ANALYTICS_GENERATED_RELATIONSHIP_DATA_FILE_PATH =
                                                                                    ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH
                                                                                        + "/analytics-relationship-data.json";   // NOSONAR

  private static final String       ANALYTICS_GENERATED_PROFILE_DATA_FILE_PATH      =
                                                                               ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH
                                                                                   + "/analytics-profile-data.json";             // NOSONAR

  private static final String       ANALYTICS_GENERATED_ACTIVITY_DATA_FILE_PATH     =
                                                                                ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH
                                                                                    + "/analytics-activity-data.json";           // NOSONAR

  private static final List<Long>   spaceIds                                        =
                                             Arrays.asList(1l, 2l, 3l, 4l, 5l, 6l, 8l, 9l, 10l, 11l, 12l, 13l, 14l, 15l, 16l);

  private static final List<Long>   USER_IDS                                        = Arrays.asList(19l,
                                                                                                    22l,
                                                                                                    24l,
                                                                                                    25l,
                                                                                                    26l,
                                                                                                    29l,
                                                                                                    41l,
                                                                                                    52l,
                                                                                                    21l,
                                                                                                    30l,
                                                                                                    32l,
                                                                                                    33l,
                                                                                                    34l,
                                                                                                    9l,
                                                                                                    42l,
                                                                                                    4l,
                                                                                                    6l,
                                                                                                    15l,
                                                                                                    27l,
                                                                                                    35l,
                                                                                                    36l,
                                                                                                    38l,
                                                                                                    16l,
                                                                                                    7l,
                                                                                                    1l,
                                                                                                    18l,
                                                                                                    28l,
                                                                                                    39l,
                                                                                                    11l,
                                                                                                    17l,
                                                                                                    31l,
                                                                                                    23l,
                                                                                                    37l);

  private static final List<String> ACTIVITY_TYPES                                  = Arrays.asList("sharefiles:spaces",
                                                                                                    "DEFAULT_ACTIVITY",
                                                                                                    "DOC_ACTIVITY",
                                                                                                    "USER_PROFILE_ACTIVITY",
                                                                                                    "files:spaces",
                                                                                                    "sharecontents:spaces",
                                                                                                    "contents:spaces",
                                                                                                    "cs-calendar:spaces",
                                                                                                    "ks-forum:spaces",
                                                                                                    "ks-answer:spaces",
                                                                                                    "ks-poll:spaces",
                                                                                                    "ks-wiki:spaces",
                                                                                                    "exokudos:activity");

  private static final List<String> ACTIVITY_COMMENT_TYPES                          =
                                                           Arrays.asList("DEFAULT_ACTIVITY", "exokudos:activity");

  private static final long         ACTIVITIES_COUNT                                = 5000l;

  private static final long[]       ACTIVITY_IDS                                    =
                                                 LongStream.range(1l, ACTIVITIES_COUNT).toArray();

  private static final List<String> PROFILE_OPERATIONS                              = Arrays.asList("avatar",
                                                                                                    "banner",
                                                                                                    "contactSection",
                                                                                                    "experienceSection");

  private static final List<String> RELATIONSHIP_OPERATIONS                         = Arrays.asList("requested",
                                                                                                    "denied",
                                                                                                    "confirmed",
                                                                                                    "ignored",
                                                                                                    "removed");

  private static final List<String> LOGIN_OPERATIONS                                = Arrays.asList("login",
                                                                                                    "logout");

  private static final List<String> SPACE_APPLICATION_OPERATIONS                    = Arrays.asList("applicationActivated",
                                                                                                    "applicationAdded",
                                                                                                    "applicationDeactivated",
                                                                                                    "applicationRemoved");

  private static final List<String> SPACE_APPLICATION_IDS                           = Arrays.asList("Forum",
                                                                                                    "Task",
                                                                                                    "ActivityStream",
                                                                                                    "Answers",
                                                                                                    "Wallet",
                                                                                                    "Documents");

  private static final List<String> SPACE_MODIFICATION_OPERATIONS                   = Arrays.asList("spaceAccessEdited",
                                                                                                    "spaceBannerEdited",
                                                                                                    "spaceRemoved",
                                                                                                    "spaceRenamed",
                                                                                                    "spaceDescriptionEdited",
                                                                                                    "spaceRegistrationEdited",
                                                                                                    "spaceAvatarEdited");

  private static final List<String> SPACE_MEMBER_MANAGEMENT_OPERATION               = Arrays.asList("grantedLead",
                                                                                                    "left",
                                                                                                    "revokedLead",
                                                                                                    "addInvitedUser",
                                                                                                    "addPendingUser");

  private static final List<String> SPACE_TEMPLATES                                 = Arrays.asList("community",
                                                                                                    "team",
                                                                                                    "collaboration",
                                                                                                    "customer");

  private static final Random       RANDOM                                          = new Random((long) (Math.random() * 10000));

  private static int                secondPerDay                                    = 24 * 3600;

  public static void main(String[] args) {
    File parentFolder = new File(ANALYTICS_GENERATED_DATA_PARENT_FOLDER_PATH);
    parentFolder.mkdir();

    File file =
              new File(ANALYTICS_GENERATED_ACTIVITY_DATA_FILE_PATH);
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      long commentIdGenerator = ACTIVITIES_COUNT;

      for (long activityId : ACTIVITY_IDS) {
        LocalDateTime date = LocalDateTime.now().minusDays(RANDOM.nextInt(120)).minusSeconds(RANDOM.nextInt(secondPerDay));

        StatisticData statisticData = new StatisticData();
        statisticData.setModule("social");
        statisticData.setSubModule("activity");
        statisticData.setOperation("createActivity");
        statisticData.setTimestamp(timeToMillis(date));

        int spaceIdIndex = RANDOM.nextInt(spaceIds.size());
        int userIdIndex = RANDOM.nextInt(USER_IDS.size());

        long spaceId = spaceIds.get(spaceIdIndex);
        long userId = USER_IDS.get(userIdIndex);

        if (activityId % 4 != 0) {
          statisticData.setSpaceId(spaceId);
        } else {
          statisticData.setUserId(userId);
        }
        statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
        statisticData.addParameter("activityType", ACTIVITY_TYPES.get(RANDOM.nextInt(ACTIVITY_TYPES.size())));
        statisticData.addParameter("activityId", activityId);
        fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());

        int numberOfComments = RANDOM.nextInt(30);

        long[] commentIds = LongStream.range(commentIdGenerator, commentIdGenerator + numberOfComments).toArray();

        for (int i = 0; i < numberOfComments; i++) {
          LocalDateTime commentDate = getDate(date);

          statisticData = new StatisticData();
          statisticData.setModule("social");
          statisticData.setSubModule("activity");
          statisticData.setOperation("createActivity");
          statisticData.setTimestamp(timeToMillis(commentDate));

          userIdIndex = RANDOM.nextInt(USER_IDS.size());
          userId = USER_IDS.get(userIdIndex);
          if (activityId % 4 != 0) {
            statisticData.setSpaceId(spaceId);
          } else {
            statisticData.setUserId(userId);
          }
          statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
          statisticData.addParameter("activityType", ACTIVITY_COMMENT_TYPES.get(RANDOM.nextInt(ACTIVITY_COMMENT_TYPES.size())));
          statisticData.addParameter("activityId", activityId);
          statisticData.addParameter("commentId", commentIdGenerator++);
          fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
        }
        for (long commentId : commentIds) {
          long numberOfLikes = RANDOM.nextInt(5);
          for (int i = 0; i < numberOfLikes; i++) {
            LocalDateTime likeDate = getDate(date);

            statisticData = new StatisticData();
            statisticData.setModule("social");
            statisticData.setSubModule("activity");
            statisticData.setOperation("likeComment");
            statisticData.setTimestamp(timeToMillis(likeDate));

            userIdIndex = RANDOM.nextInt(USER_IDS.size());
            userId = USER_IDS.get(userIdIndex);
            if (activityId % 4 != 0) {
              statisticData.setSpaceId(spaceId);
            } else {
              statisticData.setUserId(userId);
            }
            statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
            statisticData.addParameter("activityType", ACTIVITY_COMMENT_TYPES.get(RANDOM.nextInt(ACTIVITY_COMMENT_TYPES.size())));
            statisticData.addParameter("activityId", activityId);
            statisticData.addParameter("commentId", commentId);
            fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
          }
        }

        long numberOfLikes = RANDOM.nextInt(10);
        for (int i = 0; i < numberOfLikes; i++) {
          LocalDateTime likeDate = getDate(date);

          statisticData = new StatisticData();
          statisticData.setModule("social");
          statisticData.setSubModule("activity");
          statisticData.setOperation("likeActivity");
          statisticData.setTimestamp(timeToMillis(likeDate));

          userIdIndex = RANDOM.nextInt(USER_IDS.size());
          userId = USER_IDS.get(userIdIndex);
          if (activityId % 4 != 0) {
            statisticData.setSpaceId(spaceId);
          } else {
            statisticData.setUserId(userId);
          }
          statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
          statisticData.addParameter("activityId", activityId);
          fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
        }
      }
    } catch (Exception e) {
      LOG.error("Error generating activities data", e);
    }

    file =
         new File(ANALYTICS_GENERATED_PROFILE_DATA_FILE_PATH);
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

      for (long userId : USER_IDS) {
        LocalDateTime date = LocalDateTime.now().minusDays(RANDOM.nextInt(120));

        StatisticData statisticData = new StatisticData();
        statisticData.setModule("social");
        statisticData.setSubModule("profile");
        statisticData.setOperation("createProfile");
        statisticData.setTimestamp(timeToMillis(date));
        statisticData.setUserId(userId);
        statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
        fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());

        int numberOfChanges = RANDOM.nextInt(50);

        for (int i = 0; i < numberOfChanges; i++) {
          int changeIndex = RANDOM.nextInt(PROFILE_OPERATIONS.size());
          String change = PROFILE_OPERATIONS.get(changeIndex);

          LocalDateTime changeDate = date.plusDays(RANDOM.nextInt(8));

          statisticData.setModule("social");
          statisticData.setSubModule("profile");
          statisticData.setOperation(change);
          statisticData.setTimestamp(timeToMillis(changeDate));
          statisticData.setUserId(userId);
          statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
          fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
        }
      }
    } catch (Exception e) {
      LOG.error("Error generating data", e);
    }
    file = new File(ANALYTICS_GENERATED_RELATIONSHIP_DATA_FILE_PATH);
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

      for (long from : USER_IDS) {
        for (long to : USER_IDS) {
          LocalDateTime date = LocalDateTime.now().minusDays(RANDOM.nextInt(120));

          if (to == from) {
            continue;
          }
          int numberOfChanges = RANDOM.nextInt(20);

          for (int i = 0; i < numberOfChanges; i++) {
            int changeIndex = RANDOM.nextInt(RELATIONSHIP_OPERATIONS.size());
            String change = RELATIONSHIP_OPERATIONS.get(changeIndex);

            LocalDateTime changeDate = getDate(date);

            StatisticData statisticData = new StatisticData();
            statisticData.setModule("social");
            statisticData.setSubModule("relationship");
            statisticData.setOperation(change);
            statisticData.setUserId(from);
            statisticData.setTimestamp(timeToMillis(changeDate));
            statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, from);
            statisticData.addParameter("from", from);
            statisticData.addParameter("to", to);
            fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error generating relationship data", e);
    }

    file = new File(ANALYTICS_GENERATED_LOGIN_DATA_FILE_PATH);
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

      for (long userId : USER_IDS) {
        int numberOfChanges = 200;

        for (int i = 0; i < numberOfChanges; i++) {
          LocalDateTime date = LocalDateTime.now().minusDays(RANDOM.nextInt(120));

          int changeIndex = RANDOM.nextInt(LOGIN_OPERATIONS.size());
          String change = LOGIN_OPERATIONS.get(changeIndex);

          LocalDateTime changeDate = getDate(date);

          StatisticData statisticData = new StatisticData();
          statisticData.setModule("portal");
          statisticData.setSubModule("login");
          statisticData.setOperation(change);
          statisticData.setUserId(userId);
          statisticData.setTimestamp(timeToMillis(changeDate));
          fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
        }
      }
    } catch (Exception e) {
      LOG.error("Error generating login data", e);
    }

    file = new File(ANALYTICS_GENERATED_SPACE_DATA_FILE_PATH);
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

      int secondLimit = 24 * 3600;

      for (Long spaceId : spaceIds) {
        for (Long userId : USER_IDS) {
          LocalDateTime date = LocalDateTime.now().minusDays(RANDOM.nextInt(120)).minusSeconds(RANDOM.nextInt(secondLimit));

          int spaceTemplateIndex = RANDOM.nextInt(SPACE_TEMPLATES.size());
          String spaceTemplate = SPACE_TEMPLATES.get(spaceTemplateIndex);

          StatisticData statisticData = new StatisticData();
          statisticData.setModule("social");
          statisticData.setSubModule("space");
          statisticData.setOperation("joined");
          statisticData.setSpaceId(spaceId);
          statisticData.setUserId(userId);
          statisticData.setTimestamp(timeToMillis(date));
          statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
          statisticData.addParameter("spaceTemplate", spaceTemplate);
          fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());

          int numberOfChanges = RANDOM.nextInt(30);
          for (int i = 0; i < numberOfChanges; i++) {
            LocalDateTime changeDate = getDate(date);

            int applicationIdIndex = RANDOM.nextInt(SPACE_APPLICATION_IDS.size());
            int applicationOperationIndex = RANDOM.nextInt(SPACE_APPLICATION_OPERATIONS.size());

            statisticData = new StatisticData();
            statisticData.setModule("social");
            statisticData.setSubModule("space");
            statisticData.setOperation(SPACE_APPLICATION_OPERATIONS.get(applicationOperationIndex));
            statisticData.setSpaceId(spaceId);
            statisticData.setUserId(userId);
            statisticData.setTimestamp(timeToMillis(changeDate));
            statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
            statisticData.addParameter("spaceTemplate", spaceTemplate);
            statisticData.addParameter("applicationId", SPACE_APPLICATION_IDS.get(applicationIdIndex));
            fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
          }

          numberOfChanges = RANDOM.nextInt(30);
          for (int i = 0; i < numberOfChanges; i++) {
            LocalDateTime changeDate = getDate(date);

            int spaceOperationIndex = RANDOM.nextInt(SPACE_MODIFICATION_OPERATIONS.size());

            statisticData = new StatisticData();
            statisticData.setModule("social");
            statisticData.setSubModule("space");
            statisticData.setOperation(SPACE_MODIFICATION_OPERATIONS.get(spaceOperationIndex));
            statisticData.setSpaceId(spaceId);
            statisticData.setUserId(userId);
            statisticData.setTimestamp(timeToMillis(changeDate));
            statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
            statisticData.addParameter("spaceTemplate", spaceTemplate);
            fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
          }

          numberOfChanges = RANDOM.nextInt(30);
          for (int i = 0; i < numberOfChanges; i++) {
            LocalDateTime changeDate = getDate(date);

            int memberManagementIndex = RANDOM.nextInt(SPACE_MEMBER_MANAGEMENT_OPERATION.size());

            for (Long concernedUserId : USER_IDS) {
              statisticData = new StatisticData();
              statisticData.setModule("social");
              statisticData.setSubModule("space");
              statisticData.setOperation(SPACE_MEMBER_MANAGEMENT_OPERATION.get(memberManagementIndex));
              statisticData.setSpaceId(spaceId);
              statisticData.setUserId(concernedUserId);
              statisticData.setTimestamp(timeToMillis(changeDate));
              statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, userId);
              statisticData.addParameter("spaceTemplate", spaceTemplate);
              fileOutputStream.write((AnalyticsUtils.toJsonString(statisticData) + "\r\n").getBytes());
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error generating data", e);
    }
  }

  private static long timeToMillis(LocalDateTime likeDate) {
    return AnalyticsUtils.timeToMilliseconds(likeDate) + RANDOM.nextInt(100000);
  }

  private static LocalDateTime getDate(LocalDateTime date) {
    return date.plusDays(RANDOM.nextInt(8)).plusSeconds(RANDOM.nextInt(secondPerDay));
  }

}
