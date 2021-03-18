package org.exoplatform.analytics.model.filter;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregationType;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType.RANGE;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsTableFilter implements Serializable, Cloneable {

    private static final long             serialVersionUID    = 5699550622069979920L;

    private String                        title;

    private String                        chartType;

    private AnalyticsFieldFilter          scopeFilter;

    private List<String>                  colors;

    private String                        periodType;

    private LocalDate                     periodDate;

    private List<AnalyticsPercentageItemFilter>     column   = new ArrayList<>();

    private String                        lang                = null;

    public AnalyticsPeriodType getAnalyticsPeriodType() {
        return AnalyticsPeriodType.periodTypeByName(periodType);
    }
    public AnalyticsPeriod getCurrentAnalyticsPeriod() {
        AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
        if (analyticsPeriodType == null || periodDate == null) {
            return null;
        }
        return analyticsPeriodType.getCurrentPeriod(periodDate);
    }
    public AnalyticsPeriod getPreviousAnalyticsPeriod() {
        AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
        if (analyticsPeriodType == null || periodDate == null) {
            return null;
        }
        return analyticsPeriodType.getPreviousPeriod(periodDate);
    }
    private AnalyticsAggregation getXAxisAggregation() {
        AnalyticsPeriodType analyticsPeriodType = getAnalyticsPeriodType();
        if (analyticsPeriodType == null) {
            return null;
        }
        AnalyticsAggregation xAxisAggregation = new AnalyticsAggregation();
        xAxisAggregation.setField("timestamp");
        xAxisAggregation.setSortDirection("DESC");
        xAxisAggregation.setType(AnalyticsAggregationType.DATE);
        xAxisAggregation.setInterval(analyticsPeriodType.getInterval());
        return xAxisAggregation;
    }

    private AnalyticsFieldFilter getPeriodFilter() {
        AnalyticsPeriod currentAnalyticsPeriod = getCurrentAnalyticsPeriod();
        AnalyticsPeriod previousAnalyticsPeriod = getPreviousAnalyticsPeriod();
        if (previousAnalyticsPeriod == null || currentAnalyticsPeriod == null) {
            return null;
        }
        AnalyticsFilter.Range rangeFilter = new AnalyticsFilter.Range(previousAnalyticsPeriod.getFromInMS(),
                currentAnalyticsPeriod.getToInMS());
        return new AnalyticsFieldFilter("timestamp", RANGE, rangeFilter);
    }
    private AnalyticsAggregation getYAggregation(int index) {
        return column.get(index) == null ? null : column.get(index).getYAxisAggregation().clone();
    }
    private List<AnalyticsFieldFilter> getFilters(int index) {
        List<AnalyticsFieldFilter> filters = new ArrayList<>();

        AnalyticsFieldFilter periodFilter = getPeriodFilter();
        if (periodFilter != null) {
            filters.add(periodFilter);
        }
        if (scopeFilter != null) {
            filters.add(scopeFilter);
        }
        if (column.get(index) != null && column.get(index).getFilters() != null) {
            filters.addAll(column.get(index).getFilters());
        }
        return filters;
    }
    public List<AnalyticsFilter> computeFilter() {
        AnalyticsAggregation xAxisAggregation = getXAxisAggregation();
        List<AnalyticsFilter> listAnalyticsFilter = new ArrayList<>();
        for (int i =0; i< column.size(); i++){
            listAnalyticsFilter.add(new AnalyticsFilter(title,
                    chartType,
                    null,
                    getFilters(i),
                    null,
                    xAxisAggregation == null ? Collections.emptyList() : Collections.singletonList(xAxisAggregation),
                    getYAggregation(i),
                    lang,
                    0l,
                    0l));
        }
        return listAnalyticsFilter;
    }
    @Override
    public AnalyticsTableFilter clone() { // NOSONAR
        LocalDate clonedPeriodDate = periodDate == null ? null : LocalDate.from(periodDate);
        AnalyticsFieldFilter clonedScopeFilter = scopeFilter == null ? null : scopeFilter.clone();
        List<AnalyticsPercentageItemFilter> cloneColumn = new ArrayList<>(column).stream()
                .map(aggr -> aggr.clone())
                .collect(Collectors.toList());
        return new AnalyticsTableFilter(title,chartType,clonedScopeFilter,colors,periodType,clonedPeriodDate,cloneColumn,lang);
    }
}
