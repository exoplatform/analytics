package org.exoplatform.analytics.listener.news;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.news.model.News;
import org.exoplatform.services.listener.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addSpaceStatistics;

@Asynchronous
public class AnalyticsNewsListener extends Listener<String, News> {

  private static final String CREATE_CONTENT_OPERATION_NAME  = "CreateContent";

  private static final String VIEW_CONTENT_OPERATION_NAME    = "viewContent";

  private static final String SHARE_CONTENT_OPERATION_NAME   = "shareContent";

  private static final String LIKE_CONTENT_OPERATION_NAME    = "likeContent";

  private static final String COMMENT_CONTENT_OPERATION_NAME = "commentContent";

  private IdentityManager     identityManager;

  private SpaceService        spaceService;

  @Override
  public void onEvent(Event<String, News> event) throws Exception {
    News news = event.getData();
    String operation = "";
    switch (event.getEventName()) {
    case "exo.news.postArticle":
      operation = CREATE_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.viewArticle":
      operation = VIEW_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.shareArticle":
      operation = SHARE_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.commentArticle":
      operation = COMMENT_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.likeArticle":
      operation = LIKE_CONTENT_OPERATION_NAME;
      break;
    }
    addEventStatistic(news, operation);
  }

  private void addEventStatistic(News news, String operation) {

    long userId = 0;
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getAuthor());
    if (identity != null) {
      userId = Long.parseLong(identity.getId());
    }
    StatisticData statisticData = new StatisticData();

    statisticData.setModule("contents");
    statisticData.setSubModule("contents");
    statisticData.setOperation(operation);
    if (userId > 0) {
      statisticData.setUserId(userId);
    }
    statisticData.addParameter("contentId", news.getId());
    statisticData.addParameter("contentType", "News");
    statisticData.addParameter("updatedDate", news.getUpdateDate());
    Space space = getSpaceService().getSpaceById(news.getSpaceId());
    if (space != null) {
      addSpaceStatistics(statisticData, space);
    }
    AnalyticsUtils.addStatisticData(statisticData);
  }

  public IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }

  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }
}
