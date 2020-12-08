package org.exoplatform.analytics.job;

import org.quartz.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * A job to collect statistics of users count
 */
@DisallowConcurrentExecution
public class UsersStatisticsCountJob implements Job {

  private static final Log    LOG = ExoLogger.getLogger(UsersStatisticsCountJob.class);

  private ExoContainer        container;

  private OrganizationService organizationService;

  private IdentityManager     identityManager;

  public UsersStatisticsCountJob() {
    this.container = PortalContainer.getInstance();
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    long startTime = System.currentTimeMillis();

    ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(this.container);
    try {
      ListAccess<User> allUsers = getOrganizationService().getUserHandler().findAllUsers(UserStatus.ANY);
      int allUsersCount = allUsers.getSize();

      ListAccess<Identity> enabledIdentities =
                                             getIdentityManager().getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                                               null,
                                                                                               false);
      int enabledUsersCount = enabledIdentities.getSize();
      int disabledUsersCount = allUsersCount - enabledUsersCount;

      addUsersCountStatistic("allUsers", allUsersCount, startTime);
      addUsersCountStatistic("enabledUsers", enabledUsersCount, startTime);
      addUsersCountStatistic("disabledUsers", disabledUsersCount, startTime);

      startTime = System.currentTimeMillis();
      Group externalsGroup = getOrganizationService().getGroupHandler().findGroupById("/platform/externals");
      int enabledExternalUsersCount = 0;
      if (externalsGroup != null) {
        ListAccess<Membership> externalMemberships = getOrganizationService().getMembershipHandler()
                                                                             .findAllMembershipsByGroup(externalsGroup);
        enabledExternalUsersCount = externalMemberships.getSize();
      }
      addUsersCountStatistic("enabledExternalUsers", enabledExternalUsersCount, startTime);
      addUsersCountStatistic("enabledInternalUsers", (enabledUsersCount - enabledExternalUsersCount), startTime);
    } catch (Exception e) {
      LOG.error("Error while computing users statistics", e);
    } finally {
      RequestLifeCycle.end();
      ExoContainerContext.setCurrentContainer(currentContainer);
    }
  }

  private void addUsersCountStatistic(String countType, int count, long startTime) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("portal");
    statisticData.setSubModule("account");
    statisticData.setOperation("usersCount");
    statisticData.setDuration(System.currentTimeMillis() - startTime);
    statisticData.addParameter("countType", countType);
    statisticData.addParameter("count", count);
    AnalyticsUtils.addStatisticData(statisticData);
  }

  private OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = this.container.getComponentInstanceOfType(OrganizationService.class);
    }
    return organizationService;
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = this.container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }

}
