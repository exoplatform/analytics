package org.exoplatform.analytics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
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
  private StatisticStatus     status           = StatisticStatus.OK;

  @EqualsAndHashCode.Exclude
  private String              errorMessage;

  @EqualsAndHashCode.Exclude
  private long                duration;

  @EqualsAndHashCode.Exclude
  private long                errorCode;

  @EqualsAndHashCode.Exclude
  private Map<String, String> parameters;

  public void addParameter(String key, Object value) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    parameters.put(key, String.valueOf(value));
  }

  public static enum StatisticStatus {
    OK,
    KO;
  }

}
