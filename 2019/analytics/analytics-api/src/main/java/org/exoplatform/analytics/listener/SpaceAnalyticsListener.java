package org.exoplatform.analytics.listener;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class SpaceAnalyticsListener extends SpaceListenerPlugin {

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceAccessEdited");
    addStatisticData(statisticData);
  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceBannerEdited");
    addStatisticData(statisticData);
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceCreated");
    addStatisticData(statisticData);
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceRemoved");
    addStatisticData(statisticData);
  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceRenamed");
    addStatisticData(statisticData);
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceDescriptionEdited");
    addStatisticData(statisticData);
  }

  @Override
  public void spaceRegistrationEdited(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceRegistrationEdited");
    addStatisticData(statisticData);
  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addSpaceStatisticEvent(event, "spaceAvatarEdited");
    addStatisticData(statisticData);
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addApplicationStatisticEvent(event, "applicationActivated");
    addStatisticData(statisticData);
  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addApplicationStatisticEvent(event, "applicationAdded");
    addStatisticData(statisticData);
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addApplicationStatisticEvent(event, "applicationDeactivated");
    addStatisticData(statisticData);
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addApplicationStatisticEvent(event, "applicationRemoved");
    addStatisticData(statisticData);
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addUserStatisticEvent(event, "grantedLead");
    addStatisticData(statisticData);
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addUserStatisticEvent(event, "joined");
    addStatisticData(statisticData);
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addUserStatisticEvent(event, "left");
    addStatisticData(statisticData);
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addUserStatisticEvent(event, "revokedLead");
    addStatisticData(statisticData);
  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addUserStatisticEvent(event, "addInvitedUser");
    addStatisticData(statisticData);
  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    StatisticData statisticData = addUserStatisticEvent(event, "addPendingUser");
    addStatisticData(statisticData);
  }

  private StatisticData addSpaceStatisticEvent(SpaceLifeCycleEvent event, String operation) {
    Space space = event.getSpace();

    return buildStatisticData(operation, space, 0);
  }

  private StatisticData addApplicationStatisticEvent(SpaceLifeCycleEvent event, String operation) {
    Space space = event.getSpace();
    String applicationId = event.getSource();

    StatisticData statisticData = buildStatisticData(operation, space, 0);
    statisticData.addParameter("applicationId", applicationId);
    return statisticData;
  }

  private StatisticData addUserStatisticEvent(SpaceLifeCycleEvent event, String operation) {
    Space space = event.getSpace();
    String username = event.getSource();

    return buildStatisticData(operation, space, getUserIdentityId(username));
  }

  private StatisticData buildStatisticData(String operation, Space space, long userId) {
    long spaceId = Long.parseLong(space.getId());
    long modifierUserId = getUserId(space.getEditor());

    StatisticData statisticData = new StatisticData();
    statisticData.setModule("social");
    statisticData.setSubModule("space");
    statisticData.setOperation(operation);
    statisticData.setSpaceId(spaceId);
    statisticData.setUserId(userId);
    statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, modifierUserId);
    statisticData.addParameter("spaceTemplate", space.getTemplate());
    return statisticData;
  }

  private long getUserId(String username) {
    long userId = getUserIdentityId(username);
    if (userId == 0) {
      userId = getCurrentUserIdentityId();
    }
    return userId;
  }

}
