package org.exoplatform.analytics.model.filter.aggregation;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.utils.AnalyticsUtils;

import groovy.transform.ToString;
import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsAggregation implements Serializable, Cloneable {

  private static final long             serialVersionUID       = 2130321038232532587L;

  public static final String            YEAR_INTERVAL          = "year";

  public static final String            MONTH_INTERVAL         = "month";

  public static final String            QUARTER_INTERVAL       = "quarter";

  public static final String            WEEK_INTERVAL          = "week";

  public static final String            DAY_INTERVAL           = "day";

  public static final String            HOUR_INTERVAL          = "hour";

  public static final String            MINUTE_INTERVAL        = "minute";

  public static final String            SECOND_INTERVAL        = "second";

  public static final DateTimeFormatter YEAR_DATE_FORMATTER    = DateTimeFormatter.ofPattern("uuuu");

  public static final DateTimeFormatter QUARTER_DATE_FORMATTER = DateTimeFormatter.ofPattern("QQQ uuuu");

  public static final DateTimeFormatter MONTH_DATE_FORMATTER   = DateTimeFormatter.ofPattern("MMM uuuu");

  public static final DateTimeFormatter DAY_DATE_FORMATTER     = DateTimeFormatter.ofPattern("d MMM uuuu");

  public static final DateTimeFormatter WEEK_DATE_FORMATTER    = DateTimeFormatter.ofPattern("'W'w uuuu");

  public static final DateTimeFormatter HOUR_DATE_FORMATTER    = DateTimeFormatter.ofPattern("hh a, d MMM uuuu");

  private AnalyticsAggregationType      type;

  private String                        field;

  @Exclude
  private String                        sortDirection;

  private String                        interval;

  @Exclude
  private long                          limit;

  public AnalyticsAggregation(String field) {
    this.field = field;
    this.type = AnalyticsAggregationType.TERMS;
  }

  public String getSortDirection() {
    if (sortDirection == null) {
      return type == AnalyticsAggregationType.DATE ? "asc" : "desc";
    }
    return sortDirection;
  }

  public String getLabel(String fieldValue, String lang) {
    if (type == AnalyticsAggregationType.DATE) {
      long timestamp = Long.parseLong(fieldValue);
      return formatTime(timestamp, lang);
    }
    return AnalyticsUtils.compueLabel(field, fieldValue);
  }

  private String formatTime(long timestamp, String lang) {
    Locale userLocale = StringUtils.isBlank(lang) ? Locale.getDefault() : new Locale(lang);
    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());
    DateTimeFormatter dateFormatter = null;
    switch (interval) {
      case YEAR_INTERVAL:
        dateFormatter = YEAR_DATE_FORMATTER;
        break;
      case QUARTER_INTERVAL:
        dateFormatter = QUARTER_DATE_FORMATTER;
        break;
      case MONTH_INTERVAL:
        dateFormatter = MONTH_DATE_FORMATTER;
        break;
      case WEEK_INTERVAL:
        dateFormatter = WEEK_DATE_FORMATTER;
        break;
      case DAY_INTERVAL:
        dateFormatter = DAY_DATE_FORMATTER;
        break;
      case HOUR_INTERVAL:
        dateFormatter = HOUR_DATE_FORMATTER;
        break;
      default:
        dateFormatter = DAY_DATE_FORMATTER;
    }
    return dateTime.format(dateFormatter.withLocale(userLocale));
  }

  @Override
  public AnalyticsAggregation clone() { // NOSONAR
    return new AnalyticsAggregation(type, field, sortDirection, interval, limit);
  }
}
