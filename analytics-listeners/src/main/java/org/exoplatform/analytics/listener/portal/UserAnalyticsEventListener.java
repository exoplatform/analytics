package org.exoplatform.analytics.listener.portal;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

public class UserAnalyticsEventListener extends UserEventListener {

  private static final Log  LOG                = ExoLogger.getLogger(UserAnalyticsEventListener.class);

  private ThreadLocal<Long> operationStartTime = new ThreadLocal<>();

  @Override
  @ExoTransactional
  public void preSave(User user, boolean isNew) throws Exception {
    operationStartTime.set(System.currentTimeMillis());
  }

  @Override
  @ExoTransactional
  public void preSetEnabled(User user) throws Exception {
    operationStartTime.set(System.currentTimeMillis());
  }

  @Override
  @ExoTransactional
  public void preDelete(User user) throws Exception {
    operationStartTime.set(System.currentTimeMillis());
  }

  @Override
  @ExoTransactional
  public void postSave(User user, boolean isNew) throws Exception {
    StatisticData statisticData = buildStatisticData(isNew ? "createUser" : "saveUser", user);
    addStatisticData(statisticData);
  }

  @Override
  @ExoTransactional
  public void postSetEnabled(User user) throws Exception {
    StatisticData statisticData = buildStatisticData("enableUser", user);
    addStatisticData(statisticData);
  }

  @Override
  @ExoTransactional
  public void postDelete(User user) throws Exception {
    StatisticData statisticData = buildStatisticData("deleteUser", user);
    addStatisticData(statisticData);
  }

  private StatisticData buildStatisticData(String operation, User user) {
    try {
      StatisticData statisticData = new StatisticData();
      statisticData.setModule("organization");
      statisticData.setSubModule("user");
      statisticData.setOperation(operation);
      statisticData.setDuration(getDuration());
      statisticData.setUserId(getCurrentUserIdentityId());
      statisticData.addParameter(FIELD_SOCIAL_IDENTITY_ID, getUserIdentityId(user.getUserName()));
      statisticData.addParameter("isEnabled", user.isEnabled());
      return statisticData;
    } catch (Exception e) {
      LOG.warn("Error building analytics Queue entry for operation {}", operation, e);
      return null;
    }
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
