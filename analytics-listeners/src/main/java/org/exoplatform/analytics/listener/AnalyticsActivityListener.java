package org.exoplatform.analytics.listener;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStream.Type;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class AnalyticsActivityListener extends ActivityListenerPlugin {

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    StatisticData statisticData = addActivityStatisticEvent(event, "createActivity");
    addStatisticData(statisticData);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    StatisticData statisticData = addActivityStatisticEvent(event, "updateActivity");
    addStatisticData(statisticData);
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    StatisticData statisticData = addActivityStatisticEvent(event, "createComment");
    addStatisticData(statisticData);
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent event) {
    StatisticData statisticData = addActivityStatisticEvent(event, "updateComment");
    addStatisticData(statisticData);
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
    StatisticData statisticData = addActivityStatisticEvent(event, "likeActivity");
    addLikeIdentityId(statisticData, event);
    addStatisticData(statisticData);
  }

  @Override
  public void likeComment(ActivityLifeCycleEvent event) {
    StatisticData statisticData = addActivityStatisticEvent(event, "likeComment");
    addLikeIdentityId(statisticData, event);
    addStatisticData(statisticData);
  }

  private void addLikeIdentityId(StatisticData statisticData, ActivityLifeCycleEvent event) {
    String[] likeIdentityIds = event.getActivity().getLikeIdentityIds();
    if (likeIdentityIds != null && likeIdentityIds.length > 0) {
      String likerId = likeIdentityIds[likeIdentityIds.length - 1];
      statisticData.addParameter("likeIdentityId", likerId);
    }
  }

  private StatisticData addActivityStatisticEvent(ActivityLifeCycleEvent event, String operation) {
    ExoSocialActivity activity = event.getActivity();

    String activityId = activity.getParentId() == null ? activity.getId() : activity.getParentId();
    String commentId = activity.getParentCommentId() == null ? activity.getId() : activity.getParentCommentId();
    String subCommentId = activity.getParentCommentId() == null ? null : activity.getId();

    long modifierUserId = 0;
    if (StringUtils.isNotBlank(activity.getPosterId())) {
      try {
        long identityId = Long.parseLong(activity.getPosterId());
        Identity identity = getIdentity(activity.getPosterId());
        if (identity != null && StringUtils.equals(identity.getProviderId(), OrganizationIdentityProvider.NAME)) {
          modifierUserId = identityId;
        }
      } catch (NumberFormatException e1) {
        modifierUserId = getUserIdentityId(activity.getPosterId());
      }
    }

    if (modifierUserId == 0) {
      modifierUserId = getCurrentUserIdentityId();
    }

    ActivityStream activityStream = activity.getActivityStream();
    if ((activityStream == null || activityStream.getType() == null || activityStream.getPrettyId() == null)
        && StringUtils.isNotBlank(activity.getParentId())) {
      ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
      ExoSocialActivity parentActivity = activityManager.getActivity(activity.getParentId());
      activityStream = parentActivity.getActivityStream();
    }

    long spaceId = 0;
    long userId = modifierUserId;
    long streamIdentityId = 0;
    Identity streamIdentity = null;
    if (activityStream != null) {
      Type type = activityStream.getType();
      boolean isSpace = type == Type.SPACE;
      String streamProviderId = isSpace ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
      String streamRemoteId = activityStream.getPrettyId();
      try {
        streamIdentity = getIdentity(streamProviderId, streamRemoteId);
      } catch (Exception e) {
        streamIdentity = getIdentity(activityStream.getId());
      }
    }

    if (streamIdentity != null) {
      streamIdentityId = Long.parseLong(streamIdentity.getId());
      if (StringUtils.equals(streamIdentity.getProviderId(), SpaceIdentityProvider.NAME)) {
        SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
        Space space = spaceService.getSpaceByPrettyName(streamIdentity.getRemoteId());
        spaceId = space == null ? 0 : Long.parseLong(space.getId());
      } else {
        userId = streamIdentityId;
      }
    }

    StatisticData statisticData = new StatisticData();
    statisticData.setModule("social");
    statisticData.setSubModule("activity");
    statisticData.setOperation(operation);
    statisticData.setSpaceId(spaceId);
    statisticData.setUserId(userId);
    if (modifierUserId > 0) {
      statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, modifierUserId);
    }
    statisticData.addParameter("streamIdentityId", streamIdentityId);
    statisticData.addParameter("activityType", activity.getType());
    if (StringUtils.isNotBlank(activityId)) {
      statisticData.addParameter("activityId", activityId);
    }
    if (StringUtils.isNotBlank(commentId)) {
      commentId = commentId.replace("comment", "");
      statisticData.addParameter("comment", commentId);
    }
    if (StringUtils.isNotBlank(subCommentId)) {
      subCommentId = subCommentId.replace("comment", "");
      statisticData.addParameter("subCommentId", subCommentId);
    }
    return statisticData;
  }

}
