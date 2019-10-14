package org.exoplatform.analytics.model;

import java.io.Serializable;
import java.time.*;
import java.time.temporal.IsoFields;
import java.util.HashMap;
import java.util.Map;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatisticData implements Serializable {

  private static final long   serialVersionUID = -2660993500359866340L;

  private long                timestamp;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @Getter(lombok.AccessLevel.NONE)
  @Setter(lombok.AccessLevel.NONE)
  private LocalDateTime       localDate;

  @EqualsAndHashCode.Exclude
  private long                userId;

  @EqualsAndHashCode.Exclude
  private long                spaceId;

  @EqualsAndHashCode.Exclude
  private String              module;

  @EqualsAndHashCode.Exclude
  private String              subModule;

  @EqualsAndHashCode.Exclude
  private String              operation;

  @EqualsAndHashCode.Exclude
  private StatisticStatus     status;

  @EqualsAndHashCode.Exclude
  private String              errorMessage;

  @EqualsAndHashCode.Exclude
  private long                duration;

  @EqualsAndHashCode.Exclude
  private long                errorCode;

  @EqualsAndHashCode.Exclude
  private Map<String, String> parameters;

  public int getHour() {
    return this.getLocalDate().getHour();
  }

  public int getDayOfMonth() {
    return this.getLocalDate().getDayOfMonth();
  }

  public int getDayOfWeek() {
    return this.getLocalDate().getDayOfWeek().getValue();
  }

  public int getDayOfYear() {
    return this.getLocalDate().getDayOfYear();
  }

  public int getWeek() {
    return this.getLocalDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
  }

  public int getMonth() {
    return this.getLocalDate().getMonthValue();
  }

  public int getYear() {
    return this.getLocalDate().getYear();
  }

  public void addParameter(String key, String value) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    parameters.put(key, value);
  }

  private LocalDateTime getLocalDate() {
    if (localDate == null) {
      this.localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    return localDate;
  }

}
