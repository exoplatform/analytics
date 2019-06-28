package org.exoplatform.addon.analytics.utils;

import static java.time.temporal.ChronoField.*;

import java.io.ByteArrayInputStream;
import java.time.*;
import java.time.format.*;
import java.time.temporal.IsoFields;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.ws.frameworks.json.JsonGenerator;
import org.exoplatform.ws.frameworks.json.JsonParser;
import org.exoplatform.ws.frameworks.json.impl.*;

public class AnalyticsUtils {
  public static final String            ANALYTICS_NEW_DATA_EVENT  = "exo.addon.analytics.data.new";

  public static final String            ES_ANALYTICS_PROCESSOR_ID = "exo.addon.analytics.processor.es";

  public static final JsonParser        JSON_PARSER               = new JsonParserImpl();

  public static final JsonGenerator     JSON_GENERATOR            = new JsonGeneratorImpl();

  public static final DateTimeFormatter YEAR_WEEK                 = new DateTimeFormatterBuilder()
                                                                                                  .parseCaseInsensitive()
                                                                                                  .appendValue(IsoFields.WEEK_BASED_YEAR,
                                                                                                               4,
                                                                                                               10,
                                                                                                               SignStyle.EXCEEDS_PAD)
                                                                                                  .appendLiteral("-W")
                                                                                                  .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR,
                                                                                                               2)
                                                                                                  .optionalStart()
                                                                                                  .appendOffsetId()
                                                                                                  .toFormatter();

  public static final DateTimeFormatter YEAR_MONTH                = new DateTimeFormatterBuilder()
                                                                                                  .appendValue(YEAR,
                                                                                                               4,
                                                                                                               10,
                                                                                                               SignStyle.EXCEEDS_PAD)
                                                                                                  .appendLiteral('-')
                                                                                                  .appendValue(MONTH_OF_YEAR, 2)
                                                                                                  .toFormatter();

  public static final DateTimeFormatter YEAR_MONTH_DATE_HOUR      = new DateTimeFormatterBuilder()
                                                                                                  .appendValue(YEAR,
                                                                                                               4,
                                                                                                               10,
                                                                                                               SignStyle.EXCEEDS_PAD)
                                                                                                  .appendLiteral('-')
                                                                                                  .appendValue(MONTH_OF_YEAR, 2)
                                                                                                  .appendLiteral('-')
                                                                                                  .appendValue(DAY_OF_MONTH, 2)
                                                                                                  .appendLiteral('T')
                                                                                                  .appendValue(HOUR_OF_DAY, 2)
                                                                                                  .toFormatter();

  private AnalyticsUtils() {
  }

  public static final String getYearMonthDayHour(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearMonthDayHour(date);
  }

  public static final String getYearMonthDayHour(LocalDateTime date) {
    return YEAR_MONTH_DATE_HOUR.format(date);
  }

  public static final String getYearMonthDay(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearMonthDay(date);
  }

  public static final String getYearMonthDay(LocalDateTime date) {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }

  public static final String getYearMonth(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearMonth(date);
  }

  public static final String getYearMonth(LocalDateTime date) {
    return YEAR_MONTH.format(date);
  }

  public static final String getYearWeek(long timestamp) {
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    return getYearWeek(date);
  }

  public static final String getYearWeek(LocalDateTime date) {
    return YEAR_WEEK.format(date);
  }

  public static final String toJsonString(Object object) {
    try {
      return JSON_GENERATOR.createJsonObject(object).toString();
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing object to string " + object, e);
    }
  }

  public static final <T> T fromJsonString(String value, Class<T> resultClass) {
    try {
      if (StringUtils.isBlank(value)) {
        return null;
      }
      JsonDefaultHandler jsonDefaultHandler = new JsonDefaultHandler();
      JSON_PARSER.parse(new ByteArrayInputStream(value.getBytes()), jsonDefaultHandler);
      return ObjectBuilder.createObject(resultClass, jsonDefaultHandler.getJsonObject());
    } catch (JsonException e) {
      throw new IllegalStateException("Error creating object from string : " + value, e);
    }
  }

  public static long timeToSeconds(LocalDateTime time) {
    return time.atZone(ZoneOffset.systemDefault()).toEpochSecond();
  }

}
