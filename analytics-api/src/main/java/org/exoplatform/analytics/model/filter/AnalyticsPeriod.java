package org.exoplatform.analytics.model.filter;

import java.time.LocalDate;
import java.time.ZoneOffset;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPeriod {

  private LocalDate from;

  private LocalDate to;

  public long getToInMS() {
    if (to == null) {
      return 0;
    }
    return to.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
  }

  public long getFromInMS() {
    if (from == null) {
      return 0;
    }
    return from.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
  }
}
