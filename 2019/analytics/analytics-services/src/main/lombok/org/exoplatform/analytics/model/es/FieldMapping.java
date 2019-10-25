package org.exoplatform.analytics.model.es;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;

import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldMapping {

  private static final List<String> NUMERIC_TYPES = Arrays.asList("long", "double", "float", "short", "int");

  private String                    name;

  @Exclude
  private String                    type;

  @Exclude
  private boolean                   hasKeywordSubField;

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
    if (isAggregation()) {
      if (hasKeywordSubField && StringUtils.equals(type, "text")) {
        return name + ".keyword";
      }
      return name;
    }
    return null;
  }

  public String getESQueryValue(String value) {
    if (isNumeric()) {
      return value;
    } else {
      return "\"" + value + "\"";
    }
  }
}
