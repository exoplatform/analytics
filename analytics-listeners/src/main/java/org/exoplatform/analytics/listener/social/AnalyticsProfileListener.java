package org.exoplatform.analytics.listener.social;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;

public class AnalyticsProfileListener extends ProfileListenerPlugin {

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    AvatarAttachment avatarAttachment = ((AvatarAttachment) event.getPayload().getProperty(AVATAR));
    if (avatarAttachment != null){
      StatisticData statisticData = buildStatisticData("avatar", event.getSource());
      statisticData.addParameter(IMAGE_SIZE, avatarAttachment.getSize());
      statisticData.addParameter(IMAGE_TYPE, avatarAttachment.getMimeType());
      addStatisticData(statisticData);
    }
  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {
    StatisticData statisticData = buildStatisticData("banner", event.getSource());
    addStatisticData(statisticData);
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    StatisticData statisticData = buildStatisticData("contactSection", event.getSource());
    addStatisticData(statisticData);
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    StatisticData statisticData = buildStatisticData("experienceSection", event.getSource());
    addStatisticData(statisticData);
  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    StatisticData statisticData = buildStatisticData("createProfile", event.getSource());
    addStatisticData(statisticData);
  }

  private StatisticData buildStatisticData(String operation, String username) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("social");
    statisticData.setSubModule("profile");
    statisticData.setOperation(operation);
    statisticData.setUserId(getUserIdentityId(username));
    long currentUserIdentityId = getCurrentUserIdentityId();
    if (currentUserIdentityId > 0) {
      statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, currentUserIdentityId);
    }
    return statisticData;
  }

}
