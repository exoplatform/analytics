package org.exoplatform.analytics.listener.portal;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addStatisticData;
import static org.exoplatform.analytics.utils.AnalyticsUtils.getUserIdentityId;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

import java.util.Date;

@Asynchronous
public class LoginAnalyticsListener extends Listener<ConversationRegistry, ConversationState> {
  private static final Log LOG = ExoLogger.getLogger(LoginAnalyticsListener.class);
  private static final String LOGIN = "login";

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
    ConversationState state = event.getData();
    long userId = getUserIdentityId(state);
    if (userId <= 0) {
      LOG.debug("User not found in state, userId= {} ", userId);
      return;
    }
    boolean isLogin = isLogin(event);
    String operation = isLogin ? LOGIN : "logout";
    UserImpl profile = (UserImpl) state.getAttribute("UserProfile");
    Date lastLoginTime = profile.getLastLoginTime();
    if (operation.equals(LOGIN)){
      if ((!DateUtils.isSameDay(new Date(),lastLoginTime)) || (profile.getCreatedDate().compareTo(lastLoginTime) == 0)){
      StatisticData statisticData = new StatisticData();
      statisticData.setModule("portal");
      statisticData.setSubModule(LOGIN);
      statisticData.setOperation(operation);
      statisticData.setUserId(userId);
      addStatisticData(statisticData);
      }
    } else {
      StatisticData statisticData = new StatisticData();
      statisticData.setModule("portal");
      statisticData.setSubModule(LOGIN);
      statisticData.setOperation(operation);
      statisticData.setUserId(userId);
      addStatisticData(statisticData);
    }
  }

  private boolean isLogin(Event<ConversationRegistry, ConversationState> event) {
    String eventName = event.getEventName();
    return StringUtils.equals("exo.core.security.ConversationRegistry.register", eventName);
  }

}
