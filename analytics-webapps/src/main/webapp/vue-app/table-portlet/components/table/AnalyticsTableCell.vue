<template>
  <v-progress-circular
    v-if="cellItemValue === 'loadingData'"
    size="24"
    color="primary"
    indeterminate />
  <i
    v-else-if="cellItemValue === 'errorRetrievingData'"
    :title="$t('analytics.errorRetrievingData')"
    class="uiIconColorError"></i>
  <div v-else-if="cellItemValues.length">
    <analytics-table-cell-value
      v-for="value in cellItemValues"
      :key="value"
      :item="cellItem"
      :value="value"
      :original-value="$analyticsUtils.toFixed(cellItem.value)"
      :threshold="$analyticsUtils.toFixed(cellItem.threshold)"
      :field="columnField"
      :data-type="columnDataType"
      :aggregation-type="columnAggregationType"
      :percentage="percentage"
      :labels="labels" />
    <span v-if="previousPeriod" class="ml-2">
      <analytics-table-cell-value
        v-for="previousValue in cellItemPreviousValues"
        :key="previousValue.value"
        :item="cellItem"
        :value="previousValue.percentage"
        :previous-value="percentage && `${previousValue.value}%` || previousValue.value"
        :field="columnField"
        :aggregation-type="columnAggregationType"
        :data-type="columnDataType"
        :percentage="isValueNumber"
        :labels="labels"
        compare />
    </span>
  </div>
  <div v-else>
    -
  </div>
</template>

<script>
export default {
  props: {
    header: {
      type: Object,
      default: function() {
        return null;
      },
    },
    labels: {
      type: Object,
      default: function() {
        return null;
      },
    },
    item: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    fullDateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    },
  }),
  computed: {
    column() {
      return this.header.column;
    },
    columnField() {
      return this.column && this.column.valueAggregation && this.column.valueAggregation.aggregation && this.column.valueAggregation.aggregation.field;
    },
    columnAggregationType() {
      return this.column && this.column.valueAggregation && this.column.valueAggregation.aggregation && this.column.valueAggregation.aggregation.type;
    },
    previousPeriod() {
      return this.column && this.column.previousPeriod;
    },
    percentage() {
      return this.column && this.column.thresholdAggregation;
    },
    columnDataType() {
      return this.header && this.header.dataType;
    },
    cellItem() {
      return this.item[this.header.value];
    },
    isValueNumber() {
      return this.columnDataType === 'long' || this.columnDataType === 'double' || this.columnDataType === 'int';
    },
    mainColumnValue() {
      return this.item && this.item['column0'];
    },
    mainColumnIdentity() {
      return this.mainColumnValue && this.mainColumnValue.identity;
    },
    cellItemValue() {
      return this.cellItem && this.cellItem.value;
    },
    cellItemValues() {
      if (this.column.userField) {
        return this.mainColumnIdentity && [this.mainColumnIdentity[this.column.userField]] || [];
      } else if (this.column.spaceField) {
        return this.mainColumnIdentity && [this.mainColumnIdentity[this.column.spaceField]] || [];
      } else if (this.columnAggregationType === 'TERMS' && (this.columnField === 'userId' || this.columnField === 'spaceId')) {
        return this.cellItem && (this.cellItem.identity && [this.cellItem.identity] || (this.cellItemValue && this.formatValues(this.cellItemValue))) || [];
      } else if (!this.cellItemValue || this.cellItemValue === 'loadingData' || this.cellItemValue === 'errorRetrievingData') {
        return [];
      } else {
        return this.formatValues(this.cellItem.value, this.cellItem.threshold);
      }
    },
    cellItemPreviousValues() {
      if (this.cellItem && this.cellItem.key) {
        const previousValues = this.formatValues(this.cellItem.previousValue, this.cellItem.previousThreshold);
        if (this.isValueNumber) {
          if (previousValues.length < 1 || !previousValues[0]) {
            previousValues[0] = {
              value: 0,
              percentage: 0,
            };
          } else if (this.cellItemValues.length < 1 || !this.cellItemValues[0]) {
            previousValues[0] = {
              value: previousValues[0],
              percentage: -100, // -100%
            };
          } else if (this.percentage) {
            previousValues[0] = {
              value: previousValues[0],
              percentage: (this.cellItemValues[0] - previousValues[0]),
            };
          } else {
            previousValues[0] = {
              value: previousValues[0],
              percentage: (this.cellItemValues[0] - previousValues[0]) * 100 / this.cellItemValues[0],
            };
          }
        }
        return previousValues;
      } else {
        return [];
      }
    },
  },
  methods: {
    formatValues(itemValue, itemThreshold) {
      if (this.percentage) {
        const value = itemValue && Number(itemValue) || 0;
        const threshold = this.cellItem.threshold && Number(itemThreshold) || 0;
        if (!value) {
          return [0];
        } else if (!threshold) {
          return [100];
        } else {
          return [this.$analyticsUtils.toFixed(value * 100 / threshold)];
        }
      } else if (itemValue && typeof itemValue === 'object' && itemValue.length) {
        return itemValue;
      } else if (itemValue && Number(itemValue)) {
        return [this.$analyticsUtils.toFixed(itemValue)];
      } else {
        return [itemValue];
      }
    }
  },
};
</script>