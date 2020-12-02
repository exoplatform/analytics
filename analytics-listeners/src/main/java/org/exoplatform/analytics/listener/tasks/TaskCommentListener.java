package org.exoplatform.analytics.listener.tasks;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.domain.*;
import org.exoplatform.task.legacy.service.ProjectService;
import org.exoplatform.task.legacy.service.TaskService;

/**
 * This listener is added in a synchronous way to be able to get user modifier
 * identifier. The processing will be made in an asynchronous way after that.
 */
// NOSONAR : FIXME @Asynchronous : Not used, see description
public class TaskCommentListener extends Listener<TaskService, Comment> {

  private static final Log LOG = ExoLogger.getLogger(TaskCommentListener.class);

  private PortalContainer  container;

  private ProjectService   projectService;

  private IdentityManager  identityManager;

  private SpaceService     spaceService;

  public TaskCommentListener() {
    this.container = PortalContainer.getInstance();
  }

  @Override
  public void onEvent(Event<TaskService, Comment> event) throws Exception {
    final Comment comment = event.getData();
    final Task task = comment.getTask();

    ConversationState conversationstate = ConversationState.getCurrent();
    final String modifierUsername = conversationstate == null
        || conversationstate.getIdentity() == null ? null : conversationstate.getIdentity().getUserId();

    CompletableFuture.supplyAsync(() -> {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        long userIdentityId = getUserIdentityId(modifierUsername);
        createCommentStatistic(task, comment, userIdentityId);
      } catch (Exception e) {
        LOG.warn("Error computing task statistics", e);
      } finally {
        RequestLifeCycle.end();
      }
      return null;
    });
  }

  private void createCommentStatistic(Task task, Comment comment, long userIdentityId) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("tasks");
    statisticData.setSubModule("comment");
    statisticData.setUserId(userIdentityId);
    statisticData.setOperation("taskCommented");

    Space space = getSpaceOfProject(task);
    long spaceId = 0;
    String spaceTemplate = null;
    if (space != null) {
      spaceId = Long.parseLong(space.getId());
      spaceTemplate = space.getTemplate();
    }
    statisticData.setSpaceId(spaceId);
    statisticData.addParameter("spaceTemplate", spaceTemplate);
    statisticData.addParameter("taskId", task.getId());
    appendTaskProperties(statisticData, task, null);
    addCommentProperties(statisticData, comment);

    AnalyticsUtils.addStatisticData(statisticData);
  }

  private void addCommentProperties(StatisticData statisticData, Comment comment) {
    statisticData.addParameter("commentId", comment.getId());
    statisticData.addParameter("authorId", getUserIdentityId(comment.getAuthor()));
    statisticData.addParameter("commentLength", comment.getComment() == null ? 0 : comment.getComment().length());
    statisticData.addParameter("isSubComment", comment.getParentComment() != null);
  }

  private void appendTaskProperties(StatisticData statisticData, Task task, String prefix) {
    if (task == null) {
      return;
    }
    if (prefix == null) {
      prefix = "";
    }
    Project project = task.getStatus() == null ? null : task.getStatus().getProject();
    if (project != null) {
      statisticData.addParameter(prefix + "projectId", project.getId());
    }

    statisticData.addParameter(prefix + "activityId", task.getActivityId());

    long assigneeId = getUserIdentityId(task.getAssignee());
    statisticData.addParameter(prefix + "assigneeId", assigneeId);

    List<Long> assigneeIds = new ArrayList<>();
    assigneeIds.add(assigneeId);
    Set<String> coworkers = task.getCoworker();
    if (coworkers != null && !coworkers.isEmpty()) {
      List<Long> coworkerIds = new ArrayList<>();
      coworkers.forEach(coworker -> {
        long coworderId = getUserIdentityId(coworker);
        coworkerIds.add(coworderId);
      });
      statisticData.addParameter(prefix + "coworkerIds", coworkerIds);

      assigneeIds.addAll(coworkerIds);
    }
    statisticData.addParameter(prefix + "assigneeIds", assigneeIds);

    statisticData.addParameter(prefix + "creatorId", getUserIdentityId(task.getCreatedBy()));
    statisticData.addParameter(prefix + "rank", task.getRank());
    if (task.getDueDate() != null) {
      statisticData.addParameter(prefix + "dueDate", task.getDueDate());
    }
    if (task.getCreatedTime() != null) {
      statisticData.addParameter(prefix + "createdTime", task.getCreatedTime());
    }
    if (task.getStartDate() != null) {
      statisticData.addParameter(prefix + "startDate", task.getStartDate());
    }
    if (task.getEndDate() != null) {
      statisticData.addParameter(prefix + "endDate", task.getEndDate());
    }
    if (task.getPriority() != null) {
      statisticData.addParameter(prefix + "priority", task.getPriority().name());
    }
    if (task.getTitle() != null) {
      statisticData.addParameter(prefix + "titleLength", task.getTitle().length());
    }
    if (task.getDescription() != null) {
      statisticData.addParameter(prefix + "descriptionLength", task.getDescription().length());
    }
    if (task.getStatus() != null) {
      statisticData.addParameter(prefix + "statusId", task.getStatus().getId());
      statisticData.addParameter(prefix + "statusName", task.getStatus().getName());
      statisticData.addParameter(prefix + "statusRank", task.getStatus().getRank());
    }
  }

  private Space getSpaceOfProject(Task task) {
    if (task == null) {
      return null;
    }
    Project project = task.getStatus() == null ? null : task.getStatus().getProject();
    if (project == null) {
      return null;
    }

    // NOSONAR FIXME: no relationship between Project and space, try to find it
    // with best effort
    Space space = null;

    Space spaceByManagerGroupId = null;
    for (String permission : getProjectService().getManager(project.getId())) {
      int index = permission.indexOf(':');
      if (index > -1) {
        String groupId = permission.substring(index + 1);
        spaceByManagerGroupId = getSpaceService().getSpaceByGroupId(groupId);
      }
    }

    if (spaceByManagerGroupId == null) {
      String projectName = project.getName();
      space = getSpaceService().getSpaceByDisplayName(projectName);
    } else {
      space = spaceByManagerGroupId;
    }
    return space;
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

  private ProjectService getProjectService() {
    if (projectService == null) {
      projectService = this.container.getComponentInstanceOfType(ProjectService.class);
    }
    return projectService;
  }
}
