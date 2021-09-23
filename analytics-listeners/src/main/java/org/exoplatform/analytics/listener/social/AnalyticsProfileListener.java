package org.exoplatform.analytics.listener.social;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
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
    Identity identity = getIdentity(OrganizationIdentityProvider.NAME, username);
    if (identity == null) {
      return null;
    }
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("social");
    statisticData.setSubModule("profile");
    statisticData.setOperation(operation);
    statisticData.setUserId(Long.parseLong(identity.getId()));
    statisticData.addParameter(FIELD_SOCIAL_IDENTITY_ID, Long.parseLong(identity.getId()));
    statisticData.addParameter("userCreatedDate", identity.getProfile() != null ? identity.getProfile().getCreatedTime() : 0);
    return statisticData;
  }

}
