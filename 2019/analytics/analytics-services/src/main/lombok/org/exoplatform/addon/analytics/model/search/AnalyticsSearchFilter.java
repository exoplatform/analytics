package org.exoplatform.addon.analytics.model.search;

import static org.exoplatform.addon.analytics.model.search.AnalyticsFieldFilterType.*;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.tuple.ImmutablePair;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsSearchFilter implements Serializable {

  private static final long          serialVersionUID = 1214157727402749748L;

  private List<AnalyticsFieldFilter> filters          = new ArrayList<>();

  private long                       offset           = 0;

  private long                       limit            = 0;

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
