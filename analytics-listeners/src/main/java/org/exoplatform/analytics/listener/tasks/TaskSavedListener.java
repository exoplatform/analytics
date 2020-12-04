package org.exoplatform.analytics.listener.tasks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
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
import org.exoplatform.task.legacy.service.*;

/**
 * This listener is added in a synchronous way to be able to get user modifier
 * identifier. The processing will be made in an asynchronous way after that.
 */
// NOSONAR : FIXME @Asynchronous : Not used, see description
public class TaskSavedListener extends Listener<TaskService, TaskPayload> {

  private static final Log LOG = ExoLogger.getLogger(TaskSavedListener.class);

  private PortalContainer  container;

  private ProjectService   projectService;

  private TaskService      taskService;

  private IdentityManager  identityManager;

  private SpaceService     spaceService;

  private final String     operation;

  public TaskSavedListener(InitParams params) {
    this.container = PortalContainer.getInstance();
    this.operation = params.getValueParam("operation").getValue();
  }

  @Override
  public void onEvent(Event<TaskService, TaskPayload> event) throws Exception {
    TaskPayload data = event.getData();

    final Task oldTask = data.before();
    final Task newTask = data.after();

    ConversationState conversationstate = ConversationState.getCurrent();

    final String modifierUsername = conversationstate == null
        || conversationstate.getIdentity() == null ? null : conversationstate.getIdentity().getUserId();

    CompletableFuture.supplyAsync(() -> {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        long userIdentityId = getUserIdentityId(modifierUsername);

        ListAccess<Label> taskLabelsListAccess = getTaskService().findLabelsByTask(newTask.getId(), modifierUsername);
        int labelsSize = taskLabelsListAccess.getSize();
        List<Label> taskLabels = null;
        if (labelsSize > 0) {
          taskLabels = Arrays.asList(taskLabelsListAccess.load(0, labelsSize));
        } else {
          taskLabels = Collections.emptyList();
        }

        createTaskStatistic(oldTask, newTask, taskLabels, userIdentityId);
      } catch (Exception e) {
        LOG.warn("Error computing task statistics", e);
      } finally {
        RequestLifeCycle.end();
      }
      return null;
    });
  }

  private void createTaskStatistic(Task oldTask, Task newTask, List<Label> taskLabels, long userIdentityId) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("tasks");
    statisticData.setSubModule("task");
    statisticData.setUserId(userIdentityId);

    Space space = getSpaceOfProject(newTask);
    long spaceId = 0;
    String spaceTemplate = null;
    if (space != null) {
      spaceId = Long.parseLong(space.getId());
      spaceTemplate = space.getTemplate();
    }
    statisticData.setSpaceId(spaceId);
    statisticData.addParameter("spaceTemplate", spaceTemplate);
    statisticData.addParameter("taskId", newTask.getId());

    List<Long> taskLabelIds = taskLabels.stream().map(Label::getId).collect(Collectors.toList());
    statisticData.addParameter("taskLabelIds", taskLabelIds);

    appendTaskProperties(statisticData, newTask, null);

    String taskOperation = computeTaskOperation(oldTask, newTask);

    statisticData.setOperation(taskOperation);

    AnalyticsUtils.addStatisticData(statisticData);
  }

  private String computeTaskOperation(Task oldTask, Task newTask) {
    String taskOperation = operation;
    if (oldTask != null) {
      if (isDiff(oldTask.isCompleted(), newTask.isCompleted())) {
        taskOperation = "taskCompleted";
      } else if (isDiff(oldTask.getDescription(), newTask.getDescription())) {
        taskOperation = "taskDescriptionChanged";
      } else if (isDiff(oldTask.getTitle(), newTask.getTitle())) {
        taskOperation = "taskTitleChanged";
      } else if (isDiff(oldTask.getDueDate(), newTask.getDueDate())) {
        taskOperation = "taskDueDateChanged";
      } else if (isDiff(oldTask.getPriority(), newTask.getPriority())) {
        taskOperation = "taskPriorityChanged";
      } else if (isDiff(oldTask.getRank(), newTask.getRank())) {
        taskOperation = "taskRankChanged";
      } else if (isDiff(oldTask.getAssignee(), newTask.getAssignee())) {
        taskOperation = "taskAssigneeChanged";
      } else if (isDiff(oldTask.getCoworker(), newTask.getCoworker())) {
        taskOperation = "taskCoworderChanged";
      } else if (oldTask.getStatus() != null
          && newTask.getStatus() != null
          && oldTask.getStatus().getProject() != null
          && newTask.getStatus().getProject() != null
          && isDiff(oldTask.getStatus().getProject().getId(), newTask.getStatus().getProject().getId())) {
        taskOperation = "taskProjectChanged";
      } else if (oldTask.getStatus() != null
          && newTask.getStatus() != null
          && isDiff(oldTask.getStatus().getName(), newTask.getStatus().getName())) {
        taskOperation = "taskStatusChanged";
      } else if (isDiff(oldTask.getStartDate(), newTask.getStartDate()) || isDiff(oldTask.getEndDate(), newTask.getEndDate())) {
        taskOperation = "taskDatesChanged";
      }
    }
    return taskOperation;
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

  private boolean isDiff(Object before, Object after) {
    return !Objects.equals(before, after);
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

  private TaskService getTaskService() {
    if (taskService == null) {
      taskService = this.container.getComponentInstanceOfType(TaskService.class);
    }
    return taskService;
  }
}
