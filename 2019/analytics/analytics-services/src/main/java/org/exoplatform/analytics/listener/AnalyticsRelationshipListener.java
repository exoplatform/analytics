package org.exoplatform.analytics.listener;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;

public class AnalyticsRelationshipListener extends RelationshipListenerPlugin {

  @Override
  public void requested(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    StatisticData statisticData = buildStatisticData("requested",
                                                     relationship.getSender().getId(),
                                                     relationship.getReceiver().getId());
    addStatisticData(statisticData);
  }

  @Override
  public void denied(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    StatisticData statisticData = buildStatisticData("denied",
                                                     relationship.getSender().getId(),
                                                     relationship.getReceiver().getId());
    addStatisticData(statisticData);
  }

  @Override
  public void confirmed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    StatisticData statisticData = buildStatisticData("confirmed",
                                                     relationship.getSender().getId(),
                                                     relationship.getReceiver().getId());
    addStatisticData(statisticData);
  }

  @Override
  public void ignored(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    StatisticData statisticData = buildStatisticData("ignored",
                                                     relationship.getSender().getId(),
                                                     relationship.getReceiver().getId());
    addStatisticData(statisticData);
  }

  @Override
  public void removed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    StatisticData statisticData = buildStatisticData("removed",
                                                     relationship.getSender().getId(),
                                                     relationship.getReceiver().getId());
    addStatisticData(statisticData);
  }

  private StatisticData buildStatisticData(String operation, String from, String to) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("social");
    statisticData.setSubModule("relationship");
    statisticData.setOperation(operation);
    statisticData.setUserId(Long.parseLong(from));
    statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, getCurrentUserIdentityId());
    statisticData.addParameter("from", from);
    statisticData.addParameter("to", to);
    return statisticData;
  }

}
