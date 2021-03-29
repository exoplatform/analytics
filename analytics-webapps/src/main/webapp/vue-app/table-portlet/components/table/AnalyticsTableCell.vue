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
  <div v-else-if="!cellItem">
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
  <div v-else-if="header.column.previousPeriod" class="text-no-wrap">
    {{ cellItemValueNumber }}
    <span
      v-if="diffWithLastPeriod"
      :title="cellItemPreviousValueNumber"
      :class="lastPeriodComparaisonClass"
      class="ml-2">
      {{ diffSign }}
      {{ diffWithLastPeriod }}%
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
    labels() {
      return {
        CancelRequest: this.$t('profile.CancelRequest'),
        Confirm: this.$t('profile.Confirm'),
        Connect: this.$t('profile.Connect'),
        Ignore: this.$t('profile.Ignore'),
        RemoveConnection: this.$t('profile.RemoveConnection'),
        StatusTitle: this.$t('profile.StatusTitle'),
        join: this.$t('space.join'),
        leave: this.$t('space.leave'),
        members: this.$t('space.members'),
      };
    },
    cellItemValue() {
      const cellItemValue = this.item[this.header.value];
      if (cellItemValue && cellItemValue.key) {
        return cellItemValue.value;
      } else {
        return cellItemValue;
      }
    },
    cellItemValueI18N() {
      const key = `analytics.${this.cellItemValue}`;
      const i18NValue = this.$t(key);
      return i18NValue === key && this.cellItemValue || i18NValue;
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
    diffSign() {
      if (!this.header.column.previousPeriod) {
        return '';
      } else if(this.cellItemValueNumber > this.cellItemPreviousValueNumber) {
        return '+';
      } else {
        return '-';
      }
    },
    diffWithLastPeriod() {
      if (!this.cellItemPreviousValueNumber || !this.cellItemValueNumber) {
        return 100;
      }
      const diffWithLastPeriod = Math.abs((this.cellItemValueNumber - this.cellItemPreviousValueNumber) / this.cellItemPreviousValueNumber) * 100;
      return this.$analyticsUtils.toFixed(diffWithLastPeriod, 0);
    },
    lastPeriodComparaisonClass() {
      if (this.cellItemPreviousValueNumber === this.cellItemValueNumber) {
        return '';
      } else if(this.cellItemValueNumber > this.cellItemPreviousValueNumber) {
        return 'success--text';
      } else {
        return 'error--text';
      }
    },
  },
};
</script>