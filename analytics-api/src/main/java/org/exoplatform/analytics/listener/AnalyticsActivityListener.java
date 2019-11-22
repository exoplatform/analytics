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
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
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

    long modifierUserId = getUserIdentityId(activity.getPosterId());

    ActivityStream activityStream = activity.getActivityStream();
    Type type = activityStream.getType();
    boolean isSpace = type == Type.SPACE;
    String streamProviderId = isSpace ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
    String streamRemoteId = activityStream.getPrettyId();
    long streamIdentityId = getIdentityId(streamProviderId, streamRemoteId);
    long spaceId = 0;
    long userId = 0;
    if (isSpace) {
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceByPrettyName(streamRemoteId);
      spaceId = space == null ? 0 : Long.parseLong(space.getId());
    } else {
      userId = streamIdentityId;
    }

    StatisticData statisticData = new StatisticData();
    statisticData.setModule("social");
    statisticData.setSubModule("activity");
    statisticData.setOperation(operation);
    statisticData.setSpaceId(spaceId);
    statisticData.setUserId(userId);
    statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, modifierUserId);
    statisticData.addParameter("streamIdentityId", streamIdentityId);
    statisticData.addParameter("activityType", activity.getType());
    if (StringUtils.isNotBlank(activityId)) {
      statisticData.addParameter("activityId", activityId);
    }
    if (StringUtils.isNotBlank(commentId)) {
      statisticData.addParameter("commentId", commentId);
    }
    if (StringUtils.isNotBlank(subCommentId)) {
      statisticData.addParameter("subCommentId", subCommentId);
    }
    return statisticData;
  }

}
