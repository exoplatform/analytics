package org.exoplatform.analytics.listener;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.NewUserEventListener;

public class UserAnalyticsEventListener extends NewUserEventListener {

  private ThreadLocal<Long> operationStartTime = new ThreadLocal<>();

  public UserAnalyticsEventListener(InitParams params) throws Exception {
    super(params);
  }

  @Override
  public void preSave(User user, boolean isNew) throws Exception {
    operationStartTime.set(System.currentTimeMillis());
  }

  @Override
  public void preSetEnabled(User user) throws Exception {
    operationStartTime.set(System.currentTimeMillis());
  }

  @Override
  public void preDelete(User user) throws Exception {
    operationStartTime.set(System.currentTimeMillis());
  }

  @Override
  public void postSave(User user, boolean isNew) throws Exception {
    StatisticData statisticData = buildStatisticData(isNew ? "createUser" : "saveUser", user);
    addStatisticData(statisticData);
  }

  @Override
  public void postSetEnabled(User user) throws Exception {
    StatisticData statisticData = buildStatisticData("enableUser", user);
    addStatisticData(statisticData);
  }

  @Override
  public void postDelete(User user) throws Exception {
    StatisticData statisticData = buildStatisticData("deleteUser", user);
    addStatisticData(statisticData);
  }

  private StatisticData buildStatisticData(String operation, User user) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("organization");
    statisticData.setSubModule("user");
    statisticData.setOperation(operation);
    statisticData.setUserId(getUserIdentityId(user.getUserName()));
    statisticData.setDuration(getDuration());
    statisticData.addParameter("isEnabled", user.isEnabled());
    statisticData.addParameter(FIELD_MODIFIER_USER_SOCIAL_ID, getCurrentUserIdentityId());
    return statisticData;
  }

  private long getDuration() {
    Long startTime = operationStartTime.get();
    if (startTime == null) {
      return 0;
    }
    operationStartTime.remove();
    return System.currentTimeMillis() - startTime;
  }

}
