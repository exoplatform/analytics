package org.exoplatform.analytics.model.filter;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.model.filter.AnalyticsFilter.Range;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsTableFilter implements Serializable, Cloneable {

  static final long                        serialVersionUID = 8707611304110081607L;

  private String                           title;

  private AnalyticsTableColumnFilter       mainColumn       = null;

  private List<AnalyticsTableColumnFilter> columns          = new ArrayList<>();

  @Override
  public AnalyticsTableFilter clone() { // NOSONAR
    AnalyticsTableColumnFilter clonedMainColumn = mainColumn == null ? null : mainColumn.clone();
    List<AnalyticsTableColumnFilter> clonedColumns = columns == null ? null
                                                                     : columns.stream()
                                                                              .map(AnalyticsTableColumnFilter::clone)
                                                                              .collect(Collectors.toList());
    return new AnalyticsTableFilter(title, clonedMainColumn, clonedColumns);
  }

  public AnalyticsTableColumnFilter getColumnFilter(int columnIndex) {
    if (columnIndex == 0) {
      return this.mainColumn == null ? null : this.mainColumn.clone();
    } else if (columnIndex <= this.columns.size()) {
      return this.columns.get(columnIndex - 1).clone();
    }
    return null;
  }

  public AnalyticsFilter buildColumnFilter(AnalyticsPeriod period,
                                           AnalyticsPeriodType periodType,
                                           AnalyticsFieldFilter fieldFilter,
                                           int limit,
                                           String sortDirection,
                                           int columnIndex) {
    AnalyticsTableColumnFilter column = null;
    if (columnIndex == 0) {
      column = this.mainColumn == null ? null : this.mainColumn.clone();
    } else if (columnIndex <= this.columns.size()) {
      column = this.columns.get(columnIndex - 1).clone();
    }
    if (column == null) {
      throw new IllegalStateException("Column with index " + columnIndex + " doesn't exist");
    }

    List<AnalyticsAggregation> xAxisAggregations = new ArrayList<>();
    xAxisAggregations.add(mainColumn.getAggregation());
    AnalyticsAggregation yAxisAggregation = null;
    if (columnIndex > 0) {
      yAxisAggregation = column.getAggregation();
    }

    List<AnalyticsFieldFilter> filters = column.getFilters();
    if (fieldFilter != null) {
      filters.add(fieldFilter);
    }
    if (period != null) {
      addPeriodFilter(period, periodType, xAxisAggregations, filters, column);
    }

    AnalyticsAggregation lastAggregation = xAxisAggregations.get(xAxisAggregations.size() - 1);
    if (StringUtils.isNotBlank(sortDirection)) {
      lastAggregation.setSortDirection(sortDirection);
    }
    if (limit > 0) {
      lastAggregation.setLimit(limit);
    }

    return new AnalyticsFilter(null,
                               null,
                               null,
                               filters,
                               null,
                               xAxisAggregations,
                               yAxisAggregation,
                               null,
                               0,
                               limit);
  }

  public AnalyticsPeriod getCurrentPeriod(AnalyticsPeriod period, AnalyticsPeriodType periodType) {
    if (periodType == null) {
      return period;
    } else {
      return periodType.getCurrentPeriod(Instant.ofEpochMilli(period.getFromInMS()
          + (period.getToInMS() - period.getFromInMS()) / 2).atZone(ZoneOffset.UTC).toLocalDate());
    }
  }

  public AnalyticsPeriod getPreviousPeriod(AnalyticsPeriod period, AnalyticsPeriodType periodType) {
    if (periodType == null) {
      return period.previousPeriod();
    } else {
      return periodType.getPreviousPeriod(Instant.ofEpochMilli(period.getFromInMS()
          + (period.getToInMS() - period.getFromInMS()) / 2).atZone(ZoneOffset.UTC).toLocalDate());
    }
  }

  private void addPeriodFilter(AnalyticsPeriod period,
                               AnalyticsPeriodType periodType,
                               List<AnalyticsAggregation> xAxisAggregations,
                               List<AnalyticsFieldFilter> filters,
                               AnalyticsTableColumnFilter column) {
    long fromInMS = period.getFromInMS();
    long toInMS = period.getToInMS();

    if (column.isPreviousPeriod()) {
      AnalyticsPeriod previousPeriod;
      String interval;
      String offset = null;
      if (periodType == null) {
        previousPeriod = period.previousPeriod();
        fromInMS = previousPeriod.getFromInMS();
        interval = period.getInterval();
        long offsetLong = (fromInMS / 86400000l) % period.getDiffInDays();
        if (offsetLong > 0) {
          offset = offsetLong + "d";
        }
      } else {
        previousPeriod = periodType.getPreviousPeriod(period.getFrom());
        fromInMS = previousPeriod.getFromInMS();
        interval = periodType.getInterval();
        if (periodType.getOffset(previousPeriod.getFromInMS()) > 0) {
          offset = periodType.getOffset(previousPeriod.getFromInMS()) + "d";
        }
      }
      xAxisAggregations.add(0,
                            new AnalyticsAggregation(AnalyticsAggregationType.DATE,
                                                     "timestamp",
                                                     "asc",
                                                     interval,
                                                     offset,
                                                     2,
                                                     true,
                                                     fromInMS,
                                                     toInMS));
    }
    AnalyticsFieldFilter periodFilter = new AnalyticsFieldFilter("timestamp",
                                                                 AnalyticsFieldFilterType.RANGE,
                                                                 new Range(fromInMS, toInMS));
    filters.add(periodFilter);
  }
}
