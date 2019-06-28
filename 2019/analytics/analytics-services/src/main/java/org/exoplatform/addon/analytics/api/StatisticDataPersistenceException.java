package org.exoplatform.addon.analytics.api;

public class StatisticDataPersistenceException extends Exception {
  private static final long serialVersionUID = -4999529027444385223L;

  public StatisticDataPersistenceException() {
  }

  public StatisticDataPersistenceException(String message) {
    super(message);
  }

  public StatisticDataPersistenceException(String message, Exception exception) {
    super(message, exception);
  }
}
