package org.exoplatform.analytics.listener.wiki;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addSpaceStatistics;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.wiki.WikiException;
import org.exoplatform.wiki.mow.api.*;
import org.exoplatform.wiki.service.PageUpdateType;
import org.exoplatform.wiki.service.listener.PageWikiListener;

public class WikiPageListener extends PageWikiListener {

  private static final Log    LOG                        = ExoLogger.getLogger(WikiPageListener.class);

  private static final String WIKI_ADD_PAGE_OPERATION    = "noteCreated";

  private static final String WIKI_UPDATE_PAGE_OPERATION = "noteUpdated";

  private static final String WIKI_DELETE_PAGE_OPERATION = "noteDeleted";

  private static final String WIKI_OPEN_PAGE_TREE = "openNoteByTree";

  private static final String WIKI_OPEN_PAGE_BREAD_CRUMB = "openNoteByBreadCrumb";

  private static final String WIKI_OPEN_PAGE_TO_EDIT = "openNoteByToEdit";

  private static final String WIKI_SWITCH_TO_NEW_APP = "switchToNewNotesApp";

  private static final String WIKI_SWITCH_TO_OLD_APP = "switchToOldNotesApp";

  protected PortalContainer   container;

  protected IdentityManager   identityManager;

  protected SpaceService      spaceService;

  public WikiPageListener() {
    this.container = PortalContainer.getInstance();
  }

  @Override
  public void postAddPage(String wikiType, String wikiOwner, String pageId, Page page) throws WikiException {
    computeWikiPageStatistics(page, wikiType, wikiOwner, WIKI_ADD_PAGE_OPERATION, null);
  }

  @Override
  public void postUpdatePage(String wikiType,
                             String wikiOwner,
                             String pageId,
                             Page page,
                             PageUpdateType wikiUpdateType) throws WikiException {
    if (!(page instanceof DraftPage) && wikiUpdateType != null) {
      computeWikiPageStatistics(page, wikiType, wikiOwner, WIKI_UPDATE_PAGE_OPERATION, wikiUpdateType);
    }
  }

  @Override
  public void postDeletePage(String wikiType, String wikiOwner, String pageId, Page page) throws WikiException {
    computeWikiPageStatistics(page, wikiType, wikiOwner, WIKI_DELETE_PAGE_OPERATION, null);
  }

  @Override
  public void postgetPagefromTree(String wikiType, String wikiOwner, String pageId, Page page) throws WikiException {
    computeWikiPageStatistics(page, wikiType, wikiOwner, WIKI_OPEN_PAGE_TREE, null);
  }

  @Override
  public void postgetPagefromBreadCrumb(String wikiType, String wikiOwner, String pageId, Page page) throws WikiException {
    computeWikiPageStatistics(page, wikiType, wikiOwner, WIKI_OPEN_PAGE_BREAD_CRUMB, null);
  }

  @Override
  public void postGetToEdit(String wikiType, String wikiOwner, String pageId, Page page) throws WikiException {
    computeWikiPageStatistics(page, wikiType, wikiOwner, WIKI_OPEN_PAGE_TO_EDIT, null);
  }

  @Override
  public void postSwitchToOldApp() {
    computeWikiPageStatistics(null, "", "", WIKI_SWITCH_TO_OLD_APP, null);
  }

  @Override
  public void postSwitchToNewApp() {
    computeWikiPageStatistics(null, "", "", WIKI_SWITCH_TO_NEW_APP, null);
  }

  private void computeWikiPageStatistics(Page page,
                                         String wikiType,
                                         String wikiOwner,
                                         String operation,
                                         PageUpdateType wikiUpdateType) {
    ConversationState conversationstate = ConversationState.getCurrent();
    final String modifierUsername = conversationstate == null
        || conversationstate.getIdentity() == null ? null : conversationstate.getIdentity().getUserId();

    computeWikiPageStatisticsAsync(page, wikiType, wikiOwner, modifierUsername, operation, wikiUpdateType);
  }

  private void computeWikiPageStatisticsAsync(final Page page,
                                              final String wikiType,
                                              final String wikiOwner,
                                              final String modifierUsername,
                                              final String operation,
                                              final PageUpdateType wikiUpdateType) {
    CompletableFuture.supplyAsync(() -> {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        long userIdentityId = getUserIdentityId(modifierUsername);
        createWikiPageStatistic(page, wikiType, wikiOwner, userIdentityId, operation, wikiUpdateType);
      } catch (Exception e) {
        LOG.warn("Error computing wiki statistics", e);
      } finally {
        RequestLifeCycle.end();
      }
      return null;
    });
  }

  private void createWikiPageStatistic(Page page,
                                       String wikiType,
                                       String wikiOwner,
                                       long userIdentityId,
                                       String operation,
                                       PageUpdateType wikiUpdateType) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("Note");
    statisticData.setSubModule("Note");
    statisticData.setOperation(operation);
    statisticData.setUserId(userIdentityId);

    if (StringUtils.isNotBlank(wikiOwner)
        && StringUtils.equalsIgnoreCase(WikiType.GROUP.name(), wikiType)) {
      Space space = getSpaceService().getSpaceByGroupId(wikiOwner);
      addSpaceStatistics(statisticData, space);
    }
    if (page != null) {
      statisticData.addParameter("wikiPageId", page.getId());
      statisticData.addParameter("wikiId", page.getWikiId());
      statisticData.addParameter("contentLength", page.getContent() == null ? 0 : page.getContent().length());
      statisticData.addParameter("titleLength", page.getTitle() == null ? 0 : page.getTitle().length());
      statisticData.addParameter("authorId", getUserIdentityId(page.getAuthor()));
      statisticData.addParameter("ownerId", getUserIdentityId(page.getOwner()));
      statisticData.addParameter("wikiType", page.getWikiType());
      statisticData.addParameter("createdDate", page.getCreatedDate());
    }
    if (wikiUpdateType != null) {
      statisticData.addParameter("updateType", wikiUpdateType.name());
    }

    AnalyticsUtils.addStatisticData(statisticData);
  }

  private long getUserIdentityId(final String username) {
    if (StringUtils.isBlank(username)) {
      return 0;
    }
    Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
    if (userIdentity == null) {
      return 0;
    }
    return Long.parseLong(userIdentity.getId());
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = this.container.getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = this.container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }

}
