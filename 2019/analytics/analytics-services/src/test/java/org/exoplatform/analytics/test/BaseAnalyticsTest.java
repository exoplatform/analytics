package org.exoplatform.analytics.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.*;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.api.service.StatisticDataQueueService;
import org.exoplatform.analytics.es.injection.AnalyticsDataInjector;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;

public abstract class BaseAnalyticsTest {
  private static final Log                   LOG = ExoLogger.getLogger(BaseAnalyticsTest.class);

  protected static PortalContainer           container;

  protected static AnalyticsService          analyticsService;

  protected static StatisticDataQueueService analyticsQueueService;

  protected static AnalyticsDataInjector     analyticsDataInjector;

  @BeforeClass
  public static void beforeTest() {
    try {
      // Added to force binding DB datasource in JNDI before starting
      // PortalContainer
      RootContainer rootContainer = RootContainer.getInstance();
      rootContainer.getComponentInstanceOfType(InitialContextInitializer.class);

      container = PortalContainer.getInstance();
      assertNotNull("Container shouldn't be null", container);
      assertTrue("Container should have been started", container.isStarted());
      ExoContainerContext.setCurrentContainer(container);

      analyticsService = getService(AnalyticsService.class);
      analyticsQueueService = getService(StatisticDataQueueService.class);
      analyticsDataInjector = getService(AnalyticsDataInjector.class);
    } catch (Exception e) {
      LOG.error("Error starting container", e);
    }
  }

  @Before
  public void beforeTestMethod() {
    RequestLifeCycle.begin(container);
  }

  @After
  public void afterTestMethod() {
    RequestLifeCycle.end();
  }

  public static final <T> T getService(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

}
