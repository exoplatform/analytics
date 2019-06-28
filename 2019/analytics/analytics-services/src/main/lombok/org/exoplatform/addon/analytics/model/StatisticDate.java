package org.exoplatform.addon.analytics.model;

import java.io.Serializable;
import java.time.*;
import java.time.temporal.IsoFields;

import org.exoplatform.addon.analytics.utils.AnalyticsUtils;

import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;

public class StatisticDate implements Serializable {

  private static final long serialVersionUID = 5674058129607407107L;

  @Getter
  private long              timestamp;

  @Exclude
  private LocalDateTime     localDate;

  public StatisticDate() {
  }

  public StatisticDate(long timestamp) {
    this.setTimestamp(timestamp);
  }

  public StatisticDate(LocalDateTime localDate) {
    this.localDate = localDate;
    this.timestamp = AnalyticsUtils.timeToSeconds(localDate);
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    this.localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
  }

  public int toHour() {
    return this.localDate.getHour();
  }

  public int toDay() {
    return this.localDate.getDayOfMonth();
  }

  public int toWeek() {
    return this.localDate.get(IsoFields.WEEK_BASED_YEAR);
  }

  public int toMonth() {
    return this.localDate.getMonthValue();
  }

  public int toYear() {
    return this.localDate.getYear();
  }
}
