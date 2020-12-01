package org.exoplatform.analytics.job;

import org.quartz.*;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * A job to collect statistics of users count
 */
@DisallowConcurrentExecution
public class SpacesStatisticsCountJob implements Job {

  private static final Log LOG = ExoLogger.getLogger(SpacesStatisticsCountJob.class);

  private ExoContainer     container;

  private SpaceService     spaceService;

  public SpacesStatisticsCountJob() {
    this.container = PortalContainer.getInstance();
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(this.container);
    try {
      ListAccess<Space> allSpaces = getSpaceService().getAllSpacesWithListAccess();
      int allSpacesCount = allSpaces.getSize();

      StatisticData statisticData = new StatisticData();
      statisticData.setModule("social");
      statisticData.setSubModule("space");
      statisticData.setOperation("spacesCount");
      statisticData.addParameter("allSpaces", allSpacesCount);
      AnalyticsUtils.addStatisticData(statisticData);
    } catch (Exception e) {
      LOG.error("Error while computing users statistics", e);
    } finally {
      RequestLifeCycle.end();
      ExoContainerContext.setCurrentContainer(currentContainer);
    }
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = this.container.getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

}
