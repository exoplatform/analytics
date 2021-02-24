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
        {{ $t('analytics.points', {0: diffWithLastPeriod}) }}
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
      default: function() {
        return [
          '#319ab3',
        ];
      },
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
      return Math.round(((this.currentPeriodValue || 0) / (this.currentPeriodThreshold || 1)) * 100);
    },
    lastPeriodPercentage() {
      return Math.round(((this.lastPeriodValue || 0) / (this.lastPeriodThreshold || 1)) * 100);
    },
    diffWithLastPeriod() {
      return this.currentPeriodPercentage - this.lastPeriodPercentage;
    },
    lastPeriodComparaisonClass() {
      return this.diffWithLastPeriod < 0 && 'error--text' ||  'success--text';
    },
    progressBarValueClass(){
      return (this.currentPeriodPercentage > 9 ? this.currentPeriodPercentage : 10) / 2 ;
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
        if (firstChart.aggregationResults && firstChart.aggregationResults.length === 2) {
          this.lastPeriodValue = Number(firstChart.aggregationResults[0].value);
          this.currentPeriodValue = Number(firstChart.aggregationResults[1].value);
        }
        if (secondChart.aggregationResults && secondChart.aggregationResults.length === 2) {
          this.lastPeriodThreshold = Number(secondChart.aggregationResults[0].value);
          this.currentPeriodThreshold = Number(secondChart.aggregationResults[1].value);
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