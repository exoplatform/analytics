<template>
  <v-flex class="analytics-progress-bar-chart-body pa-4 border-box-sizing">
    <v-progress-linear
      :value="currentPeriodPercentage"
      :color="chartColor"
      class="analytics-progress-bar"
      height="45">
      <h3
        v-if="initialized"
        :style="`margin-left: calc(${progressBarValueClass}% - 24px); margin-right: auto;`"
        class="font-weight-bold white--text">
        {{ currentPeriodPercentage }}%
      </h3>
    </v-progress-linear>
    <div class="text-no-wrap mt-2">
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
    colors: {
      type: Array,
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
    chartColor() {
      return this.colors && this.colors.length && this.colors[0] || '#319ab3';
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
      const charts = (chartsData && chartsData.charts) || [];
      if (charts.length === 2) {
        const firstChart = charts[0];
        const secondChart = charts[1];

        const firstChartResults = firstChart.aggregationResults || [];
        const secondChartResults = secondChart.aggregationResults || [];

        if (firstChartResults.length > 1) {
          this.lastPeriodValue = Number(firstChartResults[0].value);
          this.currentPeriodValue = Number(firstChartResults[1].value);
        } else if (firstChartResults.length > 0) {
          this.lastPeriodValue = 0;
          this.currentPeriodValue = Number(firstChartResults[0].value);
        } else {
          this.lastPeriodValue = 0;
          this.currentPeriodValue = 0;
        }

        if (secondChartResults.length > 1) {
          this.lastPeriodThreshold = Number(secondChartResults[0].value);
          this.currentPeriodThreshold = Number(secondChartResults[1].value);
        } else if (secondChartResults.length > 0) {
          this.lastPeriodThreshold = 0;
          this.currentPeriodThreshold = Number(secondChartResults[0].value);
        } else {
          this.lastPeriodThreshold = 0;
          this.currentPeriodThreshold = 0;
        }

        if (this.currentPeriodThreshold < this.currentPeriodValue) {
          // Permutation of values to get the max value in threshold
          const value = this.currentPeriodValue;
          this.currentPeriodValue = this.currentPeriodThreshold;
          this.currentPeriodThreshold = value;
        }
        if (this.lastPeriodThreshold < this.lastPeriodValue) {
          // Permutation of values to get the max value in threshold
          const value = this.lastPeriodValue;
          this.lastPeriodValue = this.lastPeriodThreshold;
          this.lastPeriodThreshold = value;
        }
        this.$nextTick().then(() => this.initialized = true);
      }
    },
  }
};
</script>