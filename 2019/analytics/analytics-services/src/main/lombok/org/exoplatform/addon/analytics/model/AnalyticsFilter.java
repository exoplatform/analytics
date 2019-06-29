package org.exoplatform.addon.analytics.model;

import static org.exoplatform.addon.analytics.model.AnalyticsFieldFilterType.*;

import java.util.*;

import org.apache.commons.lang3.tuple.ImmutablePair;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsFilter {

  private List<AnalyticsFieldFilter> filters = new ArrayList<>();

  private long                       offset  = 0;

  private long                       limit   = 0;

  public void addEqualFilter(String field, String value) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, EQUAL, value);
    this.filters.add(fieldFilter);
  }

  public void addInSetFilter(String field, String... values) {
    if (values != null && values.length > 0) {
      AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, IN_SET, new HashSet<>(Arrays.asList(values)));
      this.filters.add(fieldFilter);
    }
  }

  public void addRangeFilter(String field, long start, long end) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, RANGE, new ImmutablePair<Long, Long>(start, end));
    this.filters.add(fieldFilter);
  }

  public void addGreaterFilter(String field, long value) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, GREATER, value);
    this.filters.add(fieldFilter);
  }

  public void addLessFilter(String field, long value) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, LESS, value);
    this.filters.add(fieldFilter);
  }
}
