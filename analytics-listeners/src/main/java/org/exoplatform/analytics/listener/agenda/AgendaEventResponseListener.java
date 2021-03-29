package org.exoplatform.analytics.listener.agenda;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addSpaceStatistics;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.agenda.model.Calendar;
import org.exoplatform.agenda.model.EventAttendee;
import org.exoplatform.agenda.service.AgendaCalendarService;
import org.exoplatform.agenda.service.AgendaEventService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

@Asynchronous
public class AgendaEventResponseListener  extends Listener<Long, Object> {

  private static final String   EVENT_RESPONSE        = "eventResponse";

  private AgendaCalendarService agendaCalendarService;

  private AgendaEventService agendaEventService;

  private IdentityManager identityManager;

  @Override
  public void onEvent(Event<Long, Object> event) throws Exception {
    EventAttendee eventAttendee = (EventAttendee) event.getData();
    addEventStatistic(eventAttendee, EVENT_RESPONSE );
  }

  private void addEventStatistic(EventAttendee eventAttendee, String operation) {
    String eventResponse = eventAttendee.getResponse().getValue();
    Long eventId = eventAttendee.getEventId();
    long userId = 0;
    Identity identity = getIdentityManager().getIdentity(String.valueOf(eventAttendee.getIdentityId()));
    if (identity.getProviderId().equals(OrganizationIdentityProvider.NAME)){
      userId = eventAttendee.getIdentityId();
    }
    org.exoplatform.agenda.model.Event agendaEvent = getAgendaEventService().getEventById(eventId);
    Calendar calendar = getAgendaCalendarService().getCalendarById(agendaEvent.getCalendarId());
    Identity spaceOwnerIdentity = getIdentityManager().getIdentity(String.valueOf(calendar.getOwnerId()));
    StatisticData statisticData = new StatisticData();
    if (StringUtils.equals(spaceOwnerIdentity.getProviderId(), SpaceIdentityProvider.NAME)) {
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceByPrettyName(spaceOwnerIdentity.getRemoteId());
      addSpaceStatistics(statisticData, space);
    }
    statisticData.setModule("agenda");
    statisticData.setSubModule("event");
    statisticData.setOperation(operation);
    if (userId > 0) {
      statisticData.setUserId(userId);
    }
    statisticData.addParameter("eventId", agendaEvent.getId());
    statisticData.addParameter("eventResponse", eventResponse);
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
