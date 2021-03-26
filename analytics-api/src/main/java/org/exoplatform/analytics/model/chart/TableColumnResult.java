package org.exoplatform.analytics.model.chart;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnResult {

  private List<TableColumnItemValue> items = new ArrayList<>();

  private int                        limit;

  public void addItem(TableColumnItemValue itemValue) {
    items.add(itemValue);
  }
}
