package org.exoplatform.analytics.model.filter;

import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPeriod {

  private LocalDate from;

  private LocalDate to;

}
