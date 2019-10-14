package org.exoplatform.analytics.dummy.queue;

import java.util.*;

import org.picocontainer.Startable;

import org.exoplatform.analytics.api.processor.StatisticDataProcessorPlugin;
import org.exoplatform.analytics.api.service.AnalyticsQueueService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class DummyAnalyticsQueueService implements AnalyticsQueueService, Startable {
  private static final Log                        LOG                  = ExoLogger.getLogger(DummyAnalyticsQueueService.class);

  private ArrayList<StatisticDataProcessorPlugin> dataProcessorPlugins = new ArrayList<>();

  private Vector<StatisticData>                   statisticDatas       = new Vector<>();

  private ListenerService                         listenerService;

  public DummyAnalyticsQueueService(ListenerService listenerService) {
    this.listenerService = listenerService;
  }

  @Override
  public void start() {
    this.listenerService.addListener(AnalyticsUtils.ANALYTICS_NEW_DATA_EVENT,
                                     new StatisticDataProcessorListener(dataProcessorPlugins));
  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  @Override
  public void addProcessor(StatisticDataProcessorPlugin dataProcessorPlugin) {
    this.dataProcessorPlugins.add(dataProcessorPlugin);
  }

  @Override
  public List<StatisticDataProcessorPlugin> getProcessors() {
    return this.dataProcessorPlugins;
  }

  @Override
  public void put(StatisticData data) {
    this.statisticDatas.add(data);

    try {
      this.listenerService.broadcast(AnalyticsUtils.ANALYTICS_NEW_DATA_EVENT, this, data);
    } catch (Exception e) {
      LOG.warn("Error processing analytics data: {}", data, e);
    }
  }

  @Override
  public StatisticData get(long timestamp) {
    Iterator<StatisticData> iterator = statisticDatas.iterator();
    while (iterator.hasNext()) {
      StatisticData statisticData = iterator.next();
      if (statisticData.getTimestamp() == timestamp) {

        // FIXME this shouldn't be done that way, the statistic data should be
        // deleted and purged only once the data gets persisted and processed by
        // all processors
        iterator.remove();

        return statisticData;
      }
    }
    return null;
  }

  public void remove(StatisticData data) {
    // Creating entity into ES is async, thus, it's not possible for now to
    // remove after, it's processed
    // TODO this.statisticDatas.remove(data);
  }

  @Override
  public int queueSize() {
    return this.statisticDatas.size();
  }

  @Asynchronous
  public class StatisticDataProcessorListener extends Listener<DummyAnalyticsQueueService, StatisticData> {

    private List<StatisticDataProcessorPlugin> dataProcessorPlugins;

    public StatisticDataProcessorListener(List<StatisticDataProcessorPlugin> dataProcessorPlugins) {
      this.dataProcessorPlugins = dataProcessorPlugins;
    }

    @Override
    public void onEvent(Event<DummyAnalyticsQueueService, StatisticData> event) throws Exception {
      DummyAnalyticsQueueService analyticsQueueService = event.getSource();
      StatisticData statisticData = event.getData();
      for (StatisticDataProcessorPlugin statisticDataProcessorPlugin : dataProcessorPlugins) {
        statisticDataProcessorPlugin.process(statisticData);
      }
      analyticsQueueService.remove(statisticData);
    }
  }

}
