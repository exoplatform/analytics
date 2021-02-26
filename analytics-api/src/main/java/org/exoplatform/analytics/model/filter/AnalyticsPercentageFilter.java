package org.exoplatform.analytics.model.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.analytics.model.filter.aggregation.AnalyticsAggregation;

import groovy.transform.ToString;
import lombok.*;
import org.exoplatform.analytics.model.filter.search.AnalyticsFieldFilter;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPercentageFilter implements Serializable, Cloneable {
    private static final long             serialVersionUID  = 5699550622069979910L;

    private String                        title;

    private String                        chartType;

    private List<String>                  colors;

    private List<AnalyticsAggregation>    xAxisAggregations = new ArrayList<>();

    private AnalyticsPercentageItemFilter value;

    private AnalyticsPercentageItemFilter threshold;

    private String                        lang              = null;

    private long                          offset            = 0;

    private long                          limit             = 0;

    public List<AnalyticsAggregation> getAggregations() {
        List<AnalyticsAggregation> aggregations = new ArrayList<>();

        aggregations.addAll(xAxisAggregations);

        if (this.threshold.getYAxisAggregation() != null && StringUtils.isNotBlank(this.threshold.getYAxisAggregation().getField())) {
            aggregations.add(this.threshold.getYAxisAggregation());
        }
        if (this.value.getYAxisAggregation() != null && StringUtils.isNotBlank(this.value.getYAxisAggregation().getField())) {
            aggregations.add(this.value.getYAxisAggregation());
        }

        return Collections.unmodifiableList(aggregations);
    }

    public void addXAxisAggregation(AnalyticsAggregation aggregation) {
        xAxisAggregations.add(aggregation);
    }

    @Override
    public AnalyticsPercentageFilter clone() { // NOSONAR
        List<AnalyticsAggregation> cloneXAggs = new ArrayList<>(xAxisAggregations).stream()
                .map(aggr -> aggr.clone())
                .collect(Collectors.toList());
        AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterValue = this.value.clone() ;
        AnalyticsPercentageItemFilter cloneAnalyticsPercentageItemFilterThreshold = this.threshold.clone();
        return new AnalyticsPercentageFilter(title,
                chartType,
                colors,
                cloneXAggs,
                cloneAnalyticsPercentageItemFilterValue,
                cloneAnalyticsPercentageItemFilterThreshold,
                lang,
                offset,
                limit);
    }

}
