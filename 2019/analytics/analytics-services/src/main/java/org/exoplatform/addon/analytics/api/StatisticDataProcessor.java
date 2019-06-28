package org.exoplatform.addon.analytics.api;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.ANALYTICS_NEW_DATA_EVENT;

import java.util.ArrayList;
import java.util.List;

import org.picocontainer.Startable;

import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.addon.analytics.service.AnalyticsService;
import org.exoplatform.services.listener.*;

@Asynchronous
public abstract class StatisticDataProcessor extends Listener<AnalyticsService, StatisticData> implements Startable {
  protected AnalyticsService    analyticsService;

  protected ListenerService     listenerService;

  protected List<StatisticData> statisticDatas = new ArrayList<>();

  public StatisticDataProcessor(AnalyticsService analyticsService, ListenerService listenerService) {
    this.analyticsService = analyticsService;
    this.listenerService = listenerService;
  }

  @Override
  public void start() {
    analyticsService.registerProcessor(this);
    listenerService.addListener(ANALYTICS_NEW_DATA_EVENT, this);
  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  public abstract String getId();

  public abstract void persist(StatisticData data) throws StatisticDataPersistenceException;

  @Override
  public void onEvent(Event<AnalyticsService, StatisticData> event) {
    StatisticData data = event.getData();
    statisticDatas.add(data);
    try {
      persist(data);
      statisticDatas.remove(data);
    } catch (StatisticDataPersistenceException e) {
      throw new IllegalStateException("Error while persisting statistics data " + data, e);
    }
  }

  public StatisticData getStatisticData(long timestamp) {
    return statisticDatas.stream().filter(data -> data.getDate().getTimestamp() == timestamp).findFirst().orElse(null);
  }

}
