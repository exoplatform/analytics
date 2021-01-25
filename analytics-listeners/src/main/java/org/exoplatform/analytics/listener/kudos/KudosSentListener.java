package org.exoplatform.analytics.listener.kudos;

import static org.exoplatform.analytics.utils.AnalyticsUtils.getIdentity;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStream.Type;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImpl;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

@Asynchronous
public class KudosSentListener extends Listener<KudosService, Kudos> {

  private static final Log LOG = ExoLogger.getLogger(KudosSentListener.class);

  private PortalContainer  container;

  private ActivityManager  activityManager;

  private SpaceService     spaceService;

  public KudosSentListener() {
    this.container = PortalContainer.getInstance();
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    addEventStatistic(kudos);
  }

  private void addEventStatistic(Kudos kudos) {
    long activityId = kudos.getActivityId();
    long spaceId = 0;
    String spaceTemplate = null;
    long streamIdentityId = 0;

    if (activityId <= 0
        && (StringUtils.equals("ACTIVITY", kudos.getEntityType()) || StringUtils.equals("COMMENT", kudos.getEntityType()))) {
      activityId = Long.parseLong(kudos.getEntityId());
    }

    if (activityId > 0) {
      ExoSocialActivity activity = getActivityManager().getActivity(RDBMSActivityStorageImpl.COMMENT_PREFIX + activityId);
      if (activity == null) {
        activity = getActivityManager().getActivity(String.valueOf(activityId));
      }
      if (activity != null) {
        ExoSocialActivity parentActivity = getActivityManager().getParentActivity(activity);
        if (parentActivity != null) {
          activity = parentActivity;
        }
      }
      Identity streamIdentity = null;
      if (activity != null) {
        ActivityStream activityStream = activity.getActivityStream();
        if (activityStream != null) {
          Type type = activityStream.getType();
          boolean isSpace = type == Type.SPACE;
          String streamProviderId = isSpace ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
          String streamRemoteId = activityStream.getPrettyId();
          try {
            streamIdentity = getIdentity(streamProviderId, streamRemoteId);
          } catch (Exception e) {
            LOG.debug("Can't retrieve identity with providerId {} and remoteId {}. Attempt to retrieve it as Identity technical ID",
                      streamProviderId,
                      streamRemoteId,
                      e);
            streamIdentity = getIdentity(activityStream.getId());
          }
        }

      }

      if (streamIdentity != null) {
        streamIdentityId = Long.parseLong(streamIdentity.getId());
        if (StringUtils.equals(streamIdentity.getProviderId(), SpaceIdentityProvider.NAME)) {
          Space space = getSpaceService().getSpaceByPrettyName(streamIdentity.getRemoteId());
          spaceId = space == null ? 0 : Long.parseLong(space.getId());
          spaceTemplate = space == null ? null : space.getTemplate();
        }
      }
    }

    StatisticData statisticData =
                                new StatisticData();
    statisticData.setModule("social");
    statisticData.setSubModule("kudos");
    statisticData.setOperation("sendKudos");
    statisticData.setSpaceId(spaceId);
    statisticData.setUserId(Long.parseLong(kudos.getSenderIdentityId()));
    if (spaceTemplate != null) {
      statisticData.addParameter("spaceTemplate", spaceTemplate);
    }
    statisticData.addParameter("activityId", activityId);
    statisticData.addParameter("streamIdentityId", streamIdentityId);
    statisticData.addParameter("kudosId", kudos.getTechnicalId());
    statisticData.addParameter("senderId", kudos.getSenderIdentityId());
    statisticData.addParameter("receiverId", kudos.getReceiverIdentityId());
    statisticData.addParameter("entityId", kudos.getEntityId());
    statisticData.addParameter("entityType", kudos.getEntityType());
    statisticData.addParameter("parentEntityId", kudos.getParentEntityId());
    statisticData.addParameter("receiverType", kudos.getReceiverType());
    statisticData.addParameter("messageLength", kudos.getMessage().length());
    statisticData.addParameter("duration", kudos.getTimeInSeconds());

    AnalyticsUtils.addStatisticData(statisticData);
  }

  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = this.container.getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  public ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = this.container.getComponentInstanceOfType(ActivityManager.class);
    }
    return activityManager;
  }
}
