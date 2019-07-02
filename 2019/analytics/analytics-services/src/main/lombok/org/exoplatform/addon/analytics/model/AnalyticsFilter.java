package org.exoplatform.addon.analytics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.addon.analytics.model.aggregation.AnalyticsAggregation;
import org.exoplatform.addon.analytics.model.search.AnalyticsSearchFilter;

import groovy.transform.ToString;
import lombok.Data;

@Data
@ToString
public class AnalyticsFilter implements Serializable {

  private static final long          serialVersionUID = 5699550622069979910L;

  private AnalyticsSearchFilter      searchFilter     = new AnalyticsSearchFilter();

  private List<AnalyticsAggregation> aggregations     = new ArrayList<>();
}
