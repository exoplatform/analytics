<template>
  <v-flex class="analytics-percentage-chart-body align-center border-box-sizing">
    <h1
      v-if="initialized"
      :style="`color: ${chartColor};`"
      class="font-weight-bold display-2">
      {{ currentPeriodPercentage }}%
    </h1>
    <template v-if="isUserLimit">
      <div class="text-no-wrap mt-2">
        {{ $t('analytics.ofActiveUsers') }}
        <span class="primary--text">{{ $t('analytics.percentOfUsers', {0: percentage}) }}</span>
      </div>
    </template>
    <div class="text-no-wrap mt-1">
      <span :class="lastPeriodComparaisonClass">
        {{ $t('analytics.points', {0: diffSign, 1: diffWithLastPeriod}) }}
      </span>
      <span class="text-color">
        {{ $t('analytics.vsLastPeriod') }}
      </span>
    </div>
  </v-flex>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data () {
    return {
      initialized: false,
      lastPeriodValue: 0,
      currentPeriodValue: 0,
      lastPeriodThreshold: 0,
      currentPeriodThreshold: 0,
    };
  },
  computed: {
    hasLimit() {
      return this.settings && this.settings.percentageLimit;
    },
    isUserLimit() {
      return this.hasLimit && this.settings.percentageLimit.field === 'userId';
    },
    percentage() {
      return this.hasLimit && this.settings.percentageLimit.percentage;
    },
    chartColor() {
      return this.settings && this.settings.colors && this.settings.colors.length && this.settings.colors[0] || '#319ab3';
    },
    currentPeriodPercentage() {
      return this.currentPeriodThreshold
              && Math.round((this.currentPeriodValue / this.currentPeriodThreshold) * 100)
              || 0;
    },
    lastPeriodPercentage() {
      return this.lastPeriodThreshold
            && Math.round((this.lastPeriodValue / this.lastPeriodThreshold) * 100)
            || 0;
    },
    diffSign() {
      if (this.currentPeriodPercentage === this.lastPeriodPercentage) {
        return '';
      } else if(this.currentPeriodPercentage > this.lastPeriodPercentage) {
        return '+';
      } else {
        return '-';
      }
    },
    diffWithLastPeriod() {
      return Math.abs(this.currentPeriodPercentage - this.lastPeriodPercentage);
    },
    lastPeriodComparaisonClass() {
      if (this.currentPeriodPercentage === this.lastPeriodPercentage) {
        return '';
      } else if(this.currentPeriodPercentage > this.lastPeriodPercentage) {
        return 'success--text';
      } else {
        return 'error--text';
      }
    },
    progressBarValueClass(){
      return (this.currentPeriodPercentage > 9 ? this.currentPeriodPercentage : 10) / 2;
    }
  },
  watch: {
    data() {
      this.init();
    }
  },
  methods: {
    init(chartsData) {
      if (chartsData){
        this.currentPeriodValue = chartsData.currentPeriodValue;
        this.currentPeriodThreshold = chartsData.currentPeriodThreshold;
        this.lastPeriodValue = chartsData.previousPeriodValue;
        this.lastPeriodThreshold = chartsData.previousPeriodThreshold;
      }
      this.$nextTick().then(() => this.initialized = true);
    }
  },
};
</script>