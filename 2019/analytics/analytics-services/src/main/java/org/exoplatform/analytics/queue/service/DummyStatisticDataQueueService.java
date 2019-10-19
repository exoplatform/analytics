package org.exoplatform.analytics.queue.service;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.*;

import org.picocontainer.Startable;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.exoplatform.analytics.api.service.StatisticDataProcessorService;
import org.exoplatform.analytics.api.service.StatisticDataQueueService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.StatisticDataQueueEntry;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class DummyStatisticDataQueueService implements StatisticDataQueueService, Startable {
  private static final Log                LOG                     = ExoLogger.getLogger(DummyStatisticDataQueueService.class);

  private Vector<StatisticDataQueueEntry> statisticDatas          = new Vector<>();                                           // NOSONAR

  private StatisticDataProcessorService   statisticDataProcessorService;

  private ScheduledExecutorService        queueProcessingExecutor = null;

  private PortalContainer                 container               = null;

  public DummyStatisticDataQueueService(PortalContainer container, StatisticDataProcessorService statisticDataProcessorService) {
    this.statisticDataProcessorService = statisticDataProcessorService;
    this.container = container;

    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Analytics-ingestor-%d").build();
    queueProcessingExecutor = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
  }

  @Override
  public void start() {
    queueProcessingExecutor.scheduleAtFixedRate(() -> {
      ExoContainerContext.setCurrentContainer(this.container);
      RequestLifeCycle.begin(this.container);
      try {
        for (StatisticDataQueueEntry statisticDataQueueEntry : this.statisticDatas) {
          try {
            statisticDataProcessorService.process(statisticDataQueueEntry);
          } catch (Exception e) {
            LOG.error("Error processing statistic data", e);
          }
        }
      } finally {
        RequestLifeCycle.end();
      }
    }, 0, 10, TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    queueProcessingExecutor.shutdown();
  }

  @Override
  public void put(StatisticData data) {
    this.statisticDatas.add(new StatisticDataQueueEntry(data));
  }

  @Override
  public StatisticData get(long id) {
    Iterator<StatisticDataQueueEntry> iterator = statisticDatas.iterator();
    while (iterator.hasNext()) {
      StatisticDataQueueEntry statisticDataQueueEntry = iterator.next();
      if (statisticDataQueueEntry.getId() == id) {
        // FIXME this shouldn't be done that way, the statistic data should be
        // deleted and purged only once the data gets persisted and processed by
        // all processors
        iterator.remove();
        return statisticDataQueueEntry.getStatisticData();
      }
    }
    return null;
  }

  @Override
  public int queueSize() {
    return this.statisticDatas.size();
  }

}
