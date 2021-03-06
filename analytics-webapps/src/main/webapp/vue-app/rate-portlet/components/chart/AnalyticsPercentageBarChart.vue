<template>
  <v-flex class="analytics-progress-bar-chart-body pa-4 border-box-sizing">
    <v-progress-linear
      :value="currentPeriodPercentage"
      :color="chartColor"
      class="analytics-progress-bar"
      height="45">
      <v-tooltip v-if="initialized" bottom>
        <template v-slot:activator="{ on, attrs }">
          <h3
            :style="`margin-left: calc(${progressBarValueClass}% - 24px); margin-right: auto;`"
            class="font-weight-bold white--text"
            v-bind="attrs"
            v-on="on">
            {{ currentPeriodPercentage }}%
          </h3>
        </template>
        <span>{{ $t('analytics.percentOfValue', {0: currentPeriodValue, 1: currentPeriodThreshold}) }}</span>
      </v-tooltip>
    </v-progress-linear>
    <div class="text-no-wrap mt-2">
      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <span
            :class="lastPeriodComparaisonClass"
            v-bind="attrs"
            v-on="on">
            {{ $t('analytics.points', {0: diffSign, 1: diffWithLastPeriod}) }}
          </span>
        </template>
        <span>{{ $t('analytics.previousPeriodValue', {0: `${lastPeriodPercentage}%`}) }}</span>
      </v-tooltip>
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
      return this.$analyticsUtils.toFixed(currentPeriodPercentage);
    },
    lastPeriodPercentage() {
      const lastPeriodPercentage = this.lastPeriodThreshold
            && (Math.round((this.lastPeriodValue / this.lastPeriodThreshold) * 10000) / 100)
            || 0;
      return this.$analyticsUtils.toFixed(lastPeriodPercentage);
    },
    diffSign() {
      if (this.currentPeriodPercentage === this.lastPeriodPercentage) {
        return '';
      } else if (this.currentPeriodPercentage > this.lastPeriodPercentage) {
        return '+';
      } else {
        return '-';
      }
    },
    diffWithLastPeriod() {
      const diffWithLastPeriod = Math.abs(this.currentPeriodPercentage - this.lastPeriodPercentage);
      return this.$analyticsUtils.toFixed(diffWithLastPeriod);
    },
    lastPeriodComparaisonClass() {
      if (this.currentPeriodPercentage === this.lastPeriodPercentage) {
        return '';
      } else if (this.currentPeriodPercentage > this.lastPeriodPercentage) {
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
  },
};
</script>