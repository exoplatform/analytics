package org.exoplatform.analytics.listener.tasks;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.attachments.model.Attachment;

public class TaskAttachmentListener extends Listener<Long, Attachment> {

  private final String operation;

  public TaskAttachmentListener(InitParams params) {
    this.operation = params.getValueParam("operation").getValue();
  }

  @Override
  public void onEvent(Event<Long, Attachment> event) {
    Attachment attachment = event.getData();
    Long taskId = event.getSource();
    createTaskAttachmentStatistic(attachment, taskId);
  }

  private void createTaskAttachmentStatistic(Attachment attachment, Long taskId) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("tasks");
    statisticData.setSubModule("task");
    statisticData.setUserId(AnalyticsUtils.getUserIdentityId(attachment.getUpdater()));
    statisticData.setOperation(operation);
    statisticData.addParameter("taskId", taskId);
    statisticData.addParameter("attachmentId", attachment.getId());
    AnalyticsUtils.addStatisticData(statisticData);
  }
}
