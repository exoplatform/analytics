package org.exoplatform.analytics.model.filter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum AnalyticsPeriodType {
  LAST24H("last24h", "day"),
  LAST_WEEK("lastWeek", "week"),
  LAST_MONTH("lastMonth", "month"),
  LAST_3_MONTHS("last3Months", "90d"),
  LAST_6_MONTHS("last6Months", "180d"),
  LAST_YEAR("lastYear", "year");

  private String typeName;

  private String interval;

  private AnalyticsPeriodType(String typeName, String interval) {
    this.typeName = typeName;
    this.interval = interval;
  }

  public AnalyticsPeriod getCurrentPeriod(LocalDate date) {
    LocalDate start = null;
    LocalDate end = null;
    switch (this) {
      case LAST24H:
        return new AnalyticsPeriod(date, date.plusDays(1));
      case LAST_WEEK:
        start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        end = start.plusDays(7);
        return new AnalyticsPeriod(start, end);
      case LAST_MONTH:
        start = date.withDayOfMonth(1);
        end = start.plusMonths(1);
        return new AnalyticsPeriod(start, end);
      case LAST_3_MONTHS:
        start = date.withDayOfMonth(1).minusMonths(2);
        end = start.plusMonths(3);
        return new AnalyticsPeriod(start, end);
      case LAST_6_MONTHS:
        start = date.withDayOfMonth(1).minusMonths(5);
        end = start.plusMonths(6);
        return new AnalyticsPeriod(start, end);
      case LAST_YEAR:
        start = date.withDayOfYear(1);
        end = start.plusYears(1);
        return new AnalyticsPeriod(start, end);
      default:
        return null;
    }
  }

  public AnalyticsPeriod getPreviousPeriod(LocalDate date) {
    switch (this) {
      case LAST24H:
        return getCurrentPeriod(date.minusDays(1));
      case LAST_WEEK:
        return getCurrentPeriod(date.minusWeeks(1));
      case LAST_MONTH:
        return getCurrentPeriod(date.minusMonths(1));
      case LAST_3_MONTHS:
        return getCurrentPeriod(date.minusMonths(3));
      case LAST_6_MONTHS:
        return getCurrentPeriod(date.minusMonths(6));
      case LAST_YEAR:
        return getCurrentPeriod(date.minusYears(1));
      default:
        return null;
    }
  }

  public String getTypeName() {
    return typeName;
  }

  public String getInterval() {
    return interval;
  }

  public static AnalyticsPeriodType periodTypeByName(String typeName) {
    return Arrays.stream(values())
                 .filter(value -> StringUtils.equals(value.getTypeName(), typeName))
                 .findFirst()
                 .orElse(null);
  }
}
