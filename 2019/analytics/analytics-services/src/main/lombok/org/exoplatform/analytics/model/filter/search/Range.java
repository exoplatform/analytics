package org.exoplatform.analytics.model.filter.search;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Range implements Serializable {

  private static final long serialVersionUID = 570632355720481459L;

  private String            min;

  private String            max;

}
