package org.exoplatform.analytics.model.filter;

import java.io.Serializable;
import java.time.*;
import java.time.temporal.ChronoUnit;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPeriod implements Serializable, Cloneable {

  private static final long serialVersionUID = 2730342636949170231L;

  private LocalDate         from;

  private LocalDate         to;

  private String            interval;

  public AnalyticsPeriod(long fromInMS, long toInMS) {
    this.from = Instant.ofEpochMilli(fromInMS).atZone(ZoneOffset.UTC).toLocalDate();
    this.to = Instant.ofEpochMilli(toInMS).atZone(ZoneOffset.UTC).toLocalDate();
    this.interval = getDiffInDays() + "d";
  }

  public AnalyticsPeriod(LocalDate from, LocalDate to) {
    this.from = from;
    this.to = to;
    this.interval = getDiffInDays() + "d";
  }

  public long getToInMS() {
    if (to == null) {
      return 0;
    }
    return to.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
  }

  public long getFromInMS() {
    if (from == null) {
      return 0;
    }
    return from.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
  }

  public AnalyticsPeriod previousPeriod() {
    long diffDays = getDiffInDays();
    return new AnalyticsPeriod(from.minusDays(diffDays), to.minusDays(diffDays));
  }

  public long getDiffInDays() {
    return ChronoUnit.DAYS.between(from, to);
  }

  public boolean isInPeriod(long timestamp) {
    return timestamp >= getFromInMS() && timestamp < getToInMS();
  }

  public boolean isInPeriod(LocalDate date) {
    return (date.isAfter(from) || date.isEqual(from)) && date.isBefore(to);
  }

  @Override
  protected AnalyticsPeriod clone() {// NOSONAR
    return new AnalyticsPeriod(from, to);
  }

}
