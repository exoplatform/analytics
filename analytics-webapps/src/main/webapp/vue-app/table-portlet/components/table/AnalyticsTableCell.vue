<template>
  <v-progress-circular
    v-if="cellItem === 'loading'"
    size="24"
    color="primary"
    indeterminate />
  <i
    v-else-if="cellItem === 'error'"
    :title="$t('analytics.errorRetrievingData')"
    class="uiIconColorError"></i>
  <div v-else-if="!cellItemValue">
    -
  </div>
  <exo-user-avatar
    v-else-if="header.dataType === 'userIdentity' && cellItem.username"
    :title="cellItem.fullname"
    :username="cellItem.username"
    :fullname="cellItem.fullname"
    :url="`/portal/dw/profile/${cellItem.username}`"
    :size="32"
    :avatar-url="cellItem.avatar"
    :labels="labels"
    :external="cellItem.external"
    :retrieve-extra-information="false"
    class="analytics-table-user"
    avatar-class="border-color" />
  <exo-space-avatar
    v-else-if="header.dataType === 'spaceIdentity' && cellItem.id"
    :space="cellItem"
    :labels="labels"
    :size="32"
    avatar-class="border-color"
    class="analytics-table-space d-inline-block" />
  <date-format
    v-else-if="header.dataType === 'date' && dateCellItem"
    :value="dateCellItem"
    :format="fullDateFormat" />
  <div v-else-if="header.column.previousPeriod" class="text-wrap">
    {{ (percentage && cellItemPercentage) || (!percentage && cellItemValueNumber) || '-' }}
    {{ percentage && '%' || '' }}
    <span
      v-if="diffWithLastPeriod"
      :title="diffWithLastPeriodLabel"
      :class="lastPeriodComparaisonClass"
      class="ml-2">
      {{ diffSign }}
      {{ unsignedDiffWithLastPeriod }}%
    </span>
  </div>
  <div v-else class="text-no-wrap">
    {{ cellItemValueI18N }}
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
    cellItem() {
      return this.item[this.header.value];
    },
    previousPeriod() {
      return this.header.column && this.header.column.previousPeriod;
    },
    percentage() {
      return this.header.column && this.header.column.thresholdAggregation;
    },
    cellItemValue() {
      const cellItemValue = this.item[this.header.value];
      if (cellItemValue && cellItemValue.key) {
        return cellItemValue.value;
      } else {
        return cellItemValue;
      }
    },
    cellItemThreshold() {
      const cellItemValue = this.item[this.header.value];
      if (cellItemValue && cellItemValue.key) {
        return cellItemValue.threshold && Number(cellItemValue.threshold) || 0;
      } else {
        return 0;
      }
    },
    cellItemPreviousThreshold() {
      const cellItemValue = this.item[this.header.value];
      if (cellItemValue && cellItemValue.key) {
        return cellItemValue.previousThreshold && Number(cellItemValue.previousThreshold) || 0;
      } else {
        return 0;
      }
    },
    cellItemPercentage() {
      if (!this.percentage || !this.cellItemValueNumber) {
        return 0;
      }
      if (!this.cellItemThreshold) {
        return 100;
      }
      return this.$analyticsUtils.toFixed(this.cellItemValueNumber * 100 / this.cellItemThreshold);
    },
    cellItemPreviousPercentage() {
      if (!this.percentage || !this.cellItemPreviousValueNumber) {
        return 0;
      }
      if (!this.cellItemPreviousThreshold) {
        return 100;
      }
      return this.$analyticsUtils.toFixed(this.cellItemPreviousValueNumber * 100 / this.cellItemPreviousThreshold);
    },
    cellItemValueI18N() {
      const key = `analytics.${this.cellItemValue}`;
      const i18NValue = this.$t(key);
      return i18NValue === key ? this.cellItemValue : i18NValue;
    },
    dateCellItem() {
      const item = this.item[this.header.value];
      const value = item && item.value || item;
      if (value && !Number.isNaN(Number(value))) {
        return new Date(Number(value));
      }
      return null;
    },
    cellItemValueNumber() {
      const cellItemValue = this.item[this.header.value];
      return cellItemValue && Number(cellItemValue.value) || 0;
    },
    cellItemPreviousValueNumber() {
      const cellItemValue = this.item[this.header.value];
      return cellItemValue && Number(cellItemValue.previousValue) || 0;
    },
    unsignedDiffWithLastPeriod() {
      return this.diffWithLastPeriod && Math.abs(this.diffWithLastPeriod) || 0;
    },
    diffWithLastPeriodLabel() {
      const previousPeriod = this.percentage && this.cellItemPreviousPercentage && `${this.cellItemPreviousPercentage}%` || this.cellItemPreviousValueNumber;
      return this.$t('analytics.previousPeriodValue', {0: previousPeriod});
    },
    diffWithLastPeriod() {
      if (this.percentage) {
        if (!this.cellItemPreviousPercentage || !this.cellItemPercentage) {
          return this.cellItemPreviousPercentage && this.cellItemPercentage && 100 || 0;
        }
        const diffWithLastPeriod = this.cellItemPercentage - this.cellItemPreviousPercentage;
        return this.$analyticsUtils.toFixed(diffWithLastPeriod);
      } else {
        if (!this.cellItemPreviousValueNumber || !this.cellItemValueNumber) {
          return this.cellItemPreviousValueNumber && this.cellItemValueNumber && 100 || 0;
        }
        const diffWithLastPeriod = ((this.cellItemValueNumber - this.cellItemPreviousValueNumber) / this.cellItemPreviousValueNumber) * 100;
        return this.$analyticsUtils.toFixed(diffWithLastPeriod);
      }
    },
    diffSign() {
      if (!this.diffWithLastPeriod) {
        return '';
      } else if(this.diffWithLastPeriod > 0) {
        return '+';
      } else {
        return '-';
      }
    },
    lastPeriodComparaisonClass() {
      if (!this.diffWithLastPeriod) {
        return '';
      } else if(this.diffWithLastPeriod > 0) {
        return 'success--text';
      } else {
        return 'error--text';
      }
    },
  },
};
</script>