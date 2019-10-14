package org.exoplatform.analytics.test.mock;

import org.picocontainer.Startable;

import org.exoplatform.services.naming.InitialContextInitializer;

/**
 * Used to force contexts to be bound before starting Liquibase initializer
 */
public class InitialContextInitializerStarter implements Startable {

  public InitialContextInitializerStarter(InitialContextInitializer contextInitializer) {
  }

  @Override
  public void start() {
    // Empty start
  }

  @Override
  public void stop() {
    // Empty stop
  }

}
