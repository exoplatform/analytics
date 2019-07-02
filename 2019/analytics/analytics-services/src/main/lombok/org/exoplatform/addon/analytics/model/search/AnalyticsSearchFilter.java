package org.exoplatform.addon.analytics.model.search;

import static org.exoplatform.addon.analytics.model.search.AnalyticsFieldFilterType.*;

import java.io.Serializable;
import java.util.*;

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
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, EQUAL, String.valueOf(value));
    this.filters.add(fieldFilter);
  }

  public void addInSetFilter(String field, String... values) {
    if (values != null && values.length > 0) {
      AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field, IN_SET, new HashSet<>(Arrays.asList(values)));
      this.filters.add(fieldFilter);
    }
  }

  public void addRangeFilter(String field, double start, double end) {
    AnalyticsFieldFilter fieldFilter = new AnalyticsFieldFilter(field,
                                                                RANGE,
                                                                new Range(String.valueOf(start), String.valueOf(end)));
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
}
