package org.exoplatform.analytics.listener.agenda;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.agenda.model.Calendar;
import org.exoplatform.agenda.service.AgendaCalendarService;
import org.exoplatform.agenda.service.AgendaEventService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

@Asynchronous
public class AgendaSavedEventListener extends Listener<Long, Object> {

  private static final String   EXO_AGENDA_EVENT_CREATED_EVENT_NAME = "exo.agenda.event.created";

  private static final String   EVENT_UPDATED_OPERATION_NAME        = "eventUpdated";

  private static final String   EVENT_CREATED_OPERATION_NAME        = "eventCreated";

  private AgendaCalendarService agendaCalendarService;

  private AgendaEventService    agendaEventService;

  private IdentityManager       identityManager;

  @Override
  public void onEvent(Event<Long, Object> event) throws Exception {
    Long eventId = event.getSource();
    org.exoplatform.agenda.model.Event agendaEvent = getAgendaEventService().getEventById(eventId);

    boolean isNew = StringUtils.equals(event.getEventName(), EXO_AGENDA_EVENT_CREATED_EVENT_NAME);

    String operation = isNew ? EVENT_CREATED_OPERATION_NAME : EVENT_UPDATED_OPERATION_NAME;
    long userId = isNew ? agendaEvent.getCreatorId() : agendaEvent.getModifierId();

    addEventStatistic(agendaEvent, operation, userId);
  }

  private void addEventStatistic(org.exoplatform.agenda.model.Event event, String operation, long userId) {
    Calendar calendar = getAgendaCalendarService().getCalendarById(event.getCalendarId());
    long calendarOwnerId = calendar.getOwnerId();

    Identity spaceOwnerIdentity = getIdentityManager().getIdentity(String.valueOf(calendar.getOwnerId()));
    long spaceId = 0;
    String spaceTemplate = null;
    if (StringUtils.equals(spaceOwnerIdentity.getProviderId(), SpaceIdentityProvider.NAME)) {
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceByPrettyName(spaceOwnerIdentity.getRemoteId());
      spaceId = space == null ? 0 : Long.parseLong(space.getId());
      spaceTemplate = space == null ? null : space.getTemplate();
    } else {
      userId = calendarOwnerId;
    }

    StatisticData statisticData = new StatisticData();
    statisticData.setModule("agenda");
    statisticData.setSubModule("event");
    statisticData.setOperation(operation);
    statisticData.setSpaceId(spaceId);
    statisticData.setUserId(userId);
    statisticData.addParameter("eventId", event.getId());
    statisticData.addParameter("parentId", event.getParentId());
    statisticData.addParameter("calendarOwnerIdentityId", calendarOwnerId);
    if (spaceTemplate != null) {
      statisticData.addParameter("spaceTemplate", spaceTemplate);
    }
    statisticData.addParameter("creatorId", event.getCreatorId());
    statisticData.addParameter("modifierId", event.getModifierId());
    statisticData.addParameter("eventStatus", event.getStatus());
    statisticData.addParameter("isRecurrent", event.getRecurrence() != null);
    statisticData.addParameter("isExceptionalOccurrence", event.getOccurrence() != null);
    AnalyticsUtils.addStatisticData(statisticData);
  }

  public AgendaEventService getAgendaEventService() {
    if (agendaEventService == null) {
      agendaEventService = ExoContainerContext.getService(AgendaEventService.class);
    }
    return agendaEventService;
  }

  public AgendaCalendarService getAgendaCalendarService() {
    if (agendaCalendarService == null) {
      agendaCalendarService = ExoContainerContext.getService(AgendaCalendarService.class);
    }
    return agendaCalendarService;
  }

  public IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }
}
