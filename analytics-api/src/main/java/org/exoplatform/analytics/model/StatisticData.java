package org.exoplatform.analytics.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatisticData implements Serializable {

  private static final long               serialVersionUID = -2660993500359866340L;

  private static final int                PRIME            = 59;

  @ToString.Exclude
  private DateFormat                      dateFormat;

  @Getter
  @Setter
  private long                            timestamp;

  @Getter
  @Setter
  private long                            userId;

  @Getter
  @Setter
  private long                            spaceId;

  @Getter
  @Setter
  private String                          module;

  @Getter
  @Setter
  private String                          subModule;

  @Getter
  @Setter
  private String                          operation;

  @Getter
  @Setter
  private StatisticStatus                 status           = StatisticStatus.OK;

  @Getter
  @Setter
  private String                          errorMessage;

  @Getter
  @Setter
  private long                            duration;

  @Getter
  @Setter
  private long                            errorCode;

  @Getter
  @Setter
  private Map<String, String>             parameters;

  @Getter
  @Setter
  private Map<String, Collection<String>> listParameters;

  public void addParameter(String key, Object value) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    if (value == null) {
      return;
    }
    if (value instanceof Collection) {
      Collection<?> collection = (Collection<?>) value;
      Collection<String> values = collection.stream()
                                            .filter(Objects::nonNull)
                                            .map(this::getFieldValue)
                                            .collect(Collectors.toList());
      if (listParameters == null) {
        listParameters = new HashMap<>();
      }
      listParameters.put(key, values);
    } else {
      parameters.put(key, getFieldValue(value));
    }
  }

  private String getFieldValue(Object value) {
    if (value instanceof Date) {
      return buildDateFormat().format(value);
    } else {
      return String.valueOf(value);
    }
  }

  private DateFormat buildDateFormat() {
    if (dateFormat == null) {
      dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    }
    return dateFormat;
  }

  public enum StatisticStatus {
    OK,
    KO;
  }

  public long computeId() {
    long result = timestamp;
    result = result * PRIME + (int) (userId >>> 32 ^ userId);
    result = result * PRIME + (int) (spaceId >>> 32 ^ spaceId);
    result = result * PRIME + (module == null ? 43 : module.hashCode());
    result = result * PRIME + (subModule == null ? 43 : subModule.hashCode());
    result = result * PRIME + (operation == null ? 43 : operation.hashCode());
    result = result * PRIME + (parameters == null ? 43 : parameters.hashCode());
    return result;
  }

  public boolean equals(final java.lang.Object o) {
    if (o == this)
      return true;
    if (!(o instanceof StatisticData))
      return false;
    final StatisticData other = (StatisticData) o;
    return hashCode() == other.hashCode();
  }

  public int hashCode() {
    return (int) computeId();
  }

}
