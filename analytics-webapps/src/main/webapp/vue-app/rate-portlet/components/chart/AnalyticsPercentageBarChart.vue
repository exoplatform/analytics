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
      <span class="text-sub-title">
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
      const currentPeriodPercentage = this.currentPeriodThreshold
              && (Math.round((this.currentPeriodValue / this.currentPeriodThreshold) * 10000) / 100)
              || 0;
      return this.toFixed(currentPeriodPercentage);
    },
    lastPeriodPercentage() {
      const lastPeriodPercentage = this.lastPeriodThreshold
            && (Math.round((this.lastPeriodValue / this.lastPeriodThreshold) * 10000) / 100)
            || 0;
      return this.toFixed(lastPeriodPercentage);
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
    },
    toFixed(value, decimals) {
      return Number.parseFloat(value).toFixed(2).replace(/(\..*[1-9])0+$/, '$1').replace(/\.0*$/, '');
    }
  },
};
</script>