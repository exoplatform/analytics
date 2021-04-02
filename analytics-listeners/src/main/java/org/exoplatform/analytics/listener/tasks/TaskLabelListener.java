package org.exoplatform.analytics.listener.tasks;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.task.domain.LabelTaskMapping;

@Asynchronous
public class TaskLabelListener extends Listener<String, LabelTaskMapping> {

  private PortalContainer container;

  private final String    operation;

  public TaskLabelListener(InitParams params) {
    this.container = PortalContainer.getInstance();
    this.operation = params.getValueParam("operation").getValue();
  }

  @Override
  @ExoTransactional
  public void onEvent(Event<String, LabelTaskMapping> event) {
    RequestLifeCycle.begin(container);
    ExoContainerContext.setCurrentContainer(container);
    try {
      LabelTaskMapping labelTaskMapping = event.getData();
      String userName = event.getSource();
      createLabelStatistic(labelTaskMapping, userName);
    } finally {
      RequestLifeCycle.end();
    }
  }

  private void createLabelStatistic(LabelTaskMapping labelTaskMapping, String userName) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("tasks");
    statisticData.setSubModule("task");
    statisticData.setUserId(AnalyticsUtils.getUserIdentityId(userName));
    statisticData.setOperation(operation);
    if (labelTaskMapping.getTask() != null) {
      statisticData.addParameter("taskId", labelTaskMapping.getTask().getId());
    }
    if (labelTaskMapping.getLabel() != null) {
      statisticData.addParameter("labelId", labelTaskMapping.getLabel().getId());
      if (labelTaskMapping.getLabel().getProject() != null) {
        statisticData.addParameter("projectId", labelTaskMapping.getLabel().getProject().getId());
      }
    }
    AnalyticsUtils.addStatisticData(statisticData);
  }
}
