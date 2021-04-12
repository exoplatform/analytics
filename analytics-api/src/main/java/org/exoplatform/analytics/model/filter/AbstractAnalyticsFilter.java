package org.exoplatform.analytics.model.filter;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.apache.commons.lang3.StringUtils;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractAnalyticsFilter implements Serializable, Cloneable {

  private static final long serialVersionUID = 2676228090313108228L;

  private String            title;

  private String            timeZone;

  public ZoneId zoneId() {
    return StringUtils.isBlank(timeZone) ? ZoneOffset.UTC : ZoneId.of(timeZone);
  }
}
