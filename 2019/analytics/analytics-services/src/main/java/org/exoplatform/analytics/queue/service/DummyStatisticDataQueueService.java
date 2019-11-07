package org.exoplatform.analytics.queue.service;

import java.util.*;
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
  private static final Log                   LOG                     = ExoLogger.getLogger(DummyStatisticDataQueueService.class);

  private Map<Long, StatisticDataQueueEntry> statisticDatas          = Collections.synchronizedMap(new HashMap<>());

  private StatisticDataProcessorService      statisticDataProcessorService;

  private ScheduledExecutorService           queueProcessingExecutor = null;

  private PortalContainer                    container               = null;

  public DummyStatisticDataQueueService(PortalContainer container, StatisticDataProcessorService statisticDataProcessorService) {
    this.statisticDataProcessorService = statisticDataProcessorService;
    this.container = container;

    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Analytics-ingestor-%d").build();
    this.queueProcessingExecutor = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
  }

  @Override
  public void start() {
    // Can't be job, because each cluster node must process its in-memory queue
    queueProcessingExecutor.scheduleAtFixedRate(() -> {
      ExoContainerContext.setCurrentContainer(this.container);
      RequestLifeCycle.begin(this.container);
      try {
        processQueue();
      } finally {
        RequestLifeCycle.end();
      }
    }, 0, 10, TimeUnit.SECONDS);
  }

  @Override
  public void processQueue() {
    if (this.statisticDatas.isEmpty()) {
      return;
    }
    List<StatisticDataQueueEntry> queueEntries = new ArrayList<>(this.statisticDatas.values());
    LOG.debug("Processing {} documents", queueEntries.size());
    statisticDataProcessorService.process(queueEntries);
    for (StatisticDataQueueEntry statisticDataQueueEntry : queueEntries) {
      if (statisticDataQueueEntry.isProcessed()) {
        this.statisticDatas.remove(statisticDataQueueEntry.getId());
      }
    }
    if (this.statisticDatas.size() > 0) {
      LOG.warn("Queue has {} remaining documents not processed successfully", this.statisticDatas.size());
    }
  }

  @Override
  public void stop() {
    queueProcessingExecutor.shutdown();
  }

  @Override
  public void put(StatisticData data) {
    StatisticDataQueueEntry statisticDataQueueEntry = new StatisticDataQueueEntry(data);
    this.statisticDatas.put(statisticDataQueueEntry.getId(), statisticDataQueueEntry);
  }

  @Override
  public StatisticData get(long id) {
    StatisticDataQueueEntry statisticDataQueueEntry = statisticDatas.get(id);
    return statisticDataQueueEntry == null ? null : statisticDataQueueEntry.getStatisticData();
  }

  @Override
  public int queueSize() {
    return this.statisticDatas.size();
  }

}
