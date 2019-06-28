package org.exoplatform.addon.analytics.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.*;

import org.exoplatform.addon.analytics.service.AnalyticsDataInjector;
import org.exoplatform.addon.analytics.service.es.AnalyticsIndexingServiceConnector;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.naming.InitialContextInitializer;

public class BaseAnalyticsTest {

  protected static PortalContainer container;

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

    AnalyticsDataInjector analyticsDataInjector = getService(AnalyticsDataInjector.class);
    analyticsDataInjector.reinjectData();
  }

  @Before
  public void beforeTestMethod() {
    RequestLifeCycle.begin(container);
    // reinjectData
    IndexingService indexingService = getService(IndexingService.class);
    indexingService.reindexAll(AnalyticsIndexingServiceConnector.ES_TYPE);
  }

  @After
  public void afterTestMethod() {
    RequestLifeCycle.end();
  }

  public static final <T> T getService(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }
}
