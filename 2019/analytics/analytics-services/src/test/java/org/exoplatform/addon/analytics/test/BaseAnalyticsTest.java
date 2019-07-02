package org.exoplatform.addon.analytics.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.*;

import org.exoplatform.addon.analytics.api.service.*;
import org.exoplatform.addon.analytics.es.AnalyticsIndexingServiceConnector;
import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.naming.InitialContextInitializer;

public class BaseAnalyticsTest {

  protected static PortalContainer       container;

  protected static AnalyticsService      analyticsService;

  protected static AnalyticsQueueService analyticsQueueService;

  protected static AnalyticsDataInjector analyticsDataInjector;

  @BeforeClass
  public static void beforeTest() {
    // Added to force binding DB datasource in JNDI before starting
    // PortalContainer
    RootContainer rootContainer = RootContainer.getInstance();
    rootContainer.getComponentInstanceOfType(InitialContextInitializer.class);

    container = PortalContainer.getInstance();
    assertNotNull("Container shouldn't be null", container);
    assertTrue("Container should have been started", container.isStarted());
    ExoContainerContext.setCurrentContainer(container);

    analyticsService = getService(AnalyticsService.class);
    analyticsQueueService = getService(AnalyticsQueueService.class);
    analyticsDataInjector = getService(AnalyticsDataInjector.class);
  }

  @Before
  public void beforeTestMethod() {
    RequestLifeCycle.begin(container);
    // reinjectData
    IndexingService indexingService = getService(IndexingService.class);
    indexingService.unindexAll(AnalyticsIndexingServiceConnector.ES_TYPE);

    IndexingOperationProcessor indexingOperationProcessor = getService(IndexingOperationProcessor.class);
    indexingOperationProcessor.process();
  }

  @After
  public void afterTestMethod() {
    RequestLifeCycle.end();
  }

  public static final <T> T getService(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

  protected void processIndexingQueue() throws InterruptedException {
    IndexingOperationProcessor indexingOperationProcessor = getService(IndexingOperationProcessor.class);
    do {
      indexingOperationProcessor.process();
      Thread.sleep(1000);
    } while (analyticsQueueService.queueSize() > 0);
    indexingOperationProcessor.process();
    Thread.sleep(1000);
  }

}
