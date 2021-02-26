package org.exoplatform.analytics.model.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;

import groovy.transform.ToString;
import lombok.*;
import org.exoplatform.analytics.utils.AnalyticsUtils;

import static org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilterType.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPercentageItemFilter implements Serializable, Cloneable {

    private static final long serialVersionUID = 9137307147421155687L;

    private List<AnalyticsFieldFilter> filters = new ArrayList<>();

    private AnalyticsAggregation yAxisAggregation = null;

    public void addNotEqualFilter(String field, String value) {
        AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, NOT_EQUAL, String.valueOf(value));
        this.filters.add(fieldFilter);
    }

    public void addEqualFilter(String field, String value) {
        AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, EQUAL, String.valueOf(value));
        this.filters.add(fieldFilter);
    }

    public void addInSetFilter(String field, String... values) {
        if (values != null && values.length > 0) {
            AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field,
                    IN_SET,
                    StringUtils.join(values, AnalyticsUtils.VALUES_SEPARATOR));
            this.filters.add(fieldFilter);
        }
    }

    public void addNotInSetFilter(String field, String... values) {
        if (values != null && values.length > 0) {
            AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field,
                    NOT_IN_SET,
                    StringUtils.join(values, AnalyticsUtils.VALUES_SEPARATOR));
            this.filters.add(fieldFilter);
        }
    }

    public void addRangeFilter(String field, String start, String end) {
        AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field,
                RANGE,
                new AnalyticsFilter.Range(start, end));
        this.filters.add(fieldFilter);
    }

    public void addGreaterFilter(String field, long value) {
        AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, GREATER, String.valueOf(value));
        this.filters.add(fieldFilter);
    }

    public void addLessFilter(String field, long value) {
        AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, LESS, String.valueOf(value));
        this.filters.add(fieldFilter);
    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Range implements Serializable, Cloneable {

        private static final long serialVersionUID = 570632355720481459L;

        private String            min;

        private String            max;

        @Override
        public AnalyticsFilter.Range clone() { // NOSONAR
            return new AnalyticsFilter.Range(min, max);
        }
    }
    @Override
    public AnalyticsPercentageItemFilter clone() { // NOSONAR
        List<AnalyticsFieldFilter> cloneFilters = new ArrayList<>(filters).stream()
                .map(filter -> filter.clone())
                .collect(Collectors.toList());
        AnalyticsAggregation cloneyAggregation = yAxisAggregation.clone();
        return new AnalyticsPercentageItemFilter(cloneFilters,cloneyAggregation);
    }
}