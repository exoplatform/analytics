package org.exoplatform.analytics.model;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticFieldMapping {

  private static final List<String> NUMERIC_TYPES = Arrays.asList("long", "double", "float", "short", "int");

  private String                    name;

  @Exclude
  private String                    type;

  @Exclude
  private boolean                   hasKeywordSubField;

  @Exclude
  private boolean                   scriptedField;

  public StatisticFieldMapping(String name, String type, boolean hasKeywordSubField) {
    this.name = name;
    this.type = type;
    this.hasKeywordSubField = hasKeywordSubField;
  }

  public static final String computeESQueryValue(String value) {
    if (NumberUtils.isDigits(value)) {
      return value;
    } else {
      return "\"" + value + "\"";
    }
  }

  public boolean isNumeric() {
    return NUMERIC_TYPES.contains(type);
  }

  public boolean isKeyword() {
    return StringUtils.equals(type, "keyword") || (hasKeywordSubField && StringUtils.equals(type, "text"));
  }

  public boolean isDate() {
    return StringUtils.equals(type, "date");
  }

  public boolean isText() {
    return StringUtils.equals(type, "text");
  }

  public boolean isAggregation() {
    return isNumeric() || isDate() || isKeyword();
  }

  public String getSearchFieldName() {
    return name;
  }

  public String getAggregationFieldName() {
    if (isAggregation() && hasKeywordSubField && StringUtils.equals(type, "text")) {
      return name + ".keyword";
    }
    return name;
  }

  public String getESQueryValue(String value) {
    if (isNumeric()) {
      return value;
    } else {
      return "\"" + value + "\"";
    }
  }

}
