<template>
  <analytics-line-chart
    ref="analyticsChart"
    :data="data"
    :labels="labels"
    :chart-label="chartLabel" />
</template>

<script>
import AnalyticsLineChart from './charts/AnalyticsLineChart.vue';

export default {
  components: {
    AnalyticsLineChart,
  },
  data: () => ({
    data: [],
    labels: [],
    chartLabel: 'Collaborative publications',
  }),
  created() {
    window.setTimeout(this.init, 500);
  },
  methods: {
    init() {
      this.labels = ['01', '02', '03', '04', '05'];
      this.data = [5, 4, 9, 9, 11];
      this.$nextTick(this.$refs.analyticsChart.init);
    }
  }
};
</script>