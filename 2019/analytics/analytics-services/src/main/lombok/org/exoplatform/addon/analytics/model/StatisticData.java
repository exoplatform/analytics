package org.exoplatform.addon.analytics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.addon.analytics.utils.AnalyticsUtils;

import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatisticData implements Serializable {

  private static final long   serialVersionUID = -2660993500359866340L;

  private StatisticDate       date;

  @Exclude
  private long                userId;

  @Exclude
  private long                spaceId;

  @Exclude
  private String              module;

  @Exclude
  private String              subModule;

  @Exclude
  private String              operation;

  @Exclude
  private StatisticStatus     status;

  @Exclude
  private String              errorMessage;

  @Exclude
  private long                errorCode;

  @Exclude
  private Map<String, String> parameters;

  public static void main(String[] args) {
    Map<String, String> parameters = new HashMap<>();

    for (int i = 1; i < 120; i++) {
      LocalDateTime date = LocalDateTime.now().minusDays(i);

      int numberOfItems = (int) (Math.random() * 30);
      for (int j = 0; j < numberOfItems; j++) {
        StatisticDate statisticDate = new StatisticDate(date.minusMinutes(j * 10));
        StatisticData statisticData = new StatisticData(statisticDate,
                                                        1L,
                                                        1L,
                                                        "social",
                                                        "activityStream",
                                                        "addActivity",
                                                        StatisticStatus.OK,
                                                        null,
                                                        0L,
                                                        parameters);
        System.out.println(AnalyticsUtils.toJsonString(statisticData));
      }
    }
  }
}
