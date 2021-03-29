package org.exoplatform.analytics.model.filter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum AnalyticsPeriodType {
  TODAY("today", "day"),
  THIS_WEEK("thisWeek", "week"),
  THIS_MONTH("thisMonth", "month"),
  THIS_QUARTER("thisQuarter", "quarter"),
  THIS_SEMESTER("thisSemester", "182d"),
  THIS_YEAR("thisYear", "year");

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
      case TODAY:
        return new AnalyticsPeriod(date, date.plusDays(1), interval);
      case THIS_WEEK:
        start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        end = start.plusDays(7);
        return new AnalyticsPeriod(start, end, interval);
      case THIS_MONTH:
        start = date.withDayOfMonth(1);
        end = start.plusMonths(1);
        return new AnalyticsPeriod(start, end, interval);
      case THIS_QUARTER:
        start = date.withDayOfMonth(1).minusMonths(2);
        end = start.plusMonths(3);
        return new AnalyticsPeriod(start, end, interval);
      case THIS_SEMESTER:
        start = date.withDayOfMonth(1).minusMonths(5);
        end = start.plusMonths(6);
        return new AnalyticsPeriod(start, end, interval);
      case THIS_YEAR:
        start = date.withDayOfYear(1);
        end = start.plusYears(1);
        return new AnalyticsPeriod(start, end, interval);
      default:
        return null;
    }
  }

  public AnalyticsPeriod getPreviousPeriod(LocalDate date) {
    switch (this) {
      case TODAY:
        return getCurrentPeriod(date.minusDays(1));
      case THIS_WEEK:
        return getCurrentPeriod(date.minusWeeks(1));
      case THIS_MONTH:
        return getCurrentPeriod(date.minusMonths(1));
      case THIS_QUARTER:
        return getCurrentPeriod(date.minusMonths(3));
      case THIS_SEMESTER:
        return getCurrentPeriod(date.minusMonths(6));
      case THIS_YEAR:
        return getCurrentPeriod(date.minusYears(1));
      default:
        return null;
    }
  }

  public String getTypeName() {
    return typeName;
  }

  public static AnalyticsPeriodType periodTypeByName(String typeName) {
    return Arrays.stream(values())
                 .filter(value -> StringUtils.equals(value.getTypeName(), typeName))
                 .findFirst()
                 .orElse(null);
  }
}
