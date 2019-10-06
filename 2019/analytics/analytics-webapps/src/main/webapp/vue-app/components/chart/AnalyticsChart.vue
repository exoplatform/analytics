<template>
  <v-flex :id="id" style="width:600px; height:400px;" />
</template>
<script>
export default {
  data () {
    return {
      id: `Chart${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
    }
  },
  watch: {
    data() {
      this.init();
    }
  },
  methods: {
    init(chartsData, settings) {
      if (!chartsData || !chartsData.charts || !chartsData.charts.length) {
        console.debug('No chart data', chartsData);
        return;
      }

      const $container = $(`#${this.id}`);
      if (!$container.length) {
        console.debug('No chart container for selector', this.id);
        return;
      }

      const series = [];
      chartsData.charts.forEach(chartData => {
        const serie = {
            type: settings && settings.chartType || 'bar',
            data: chartData.data,
        };
        if (chartData.chartKey) {
          serie.name = chartData.chartKey;
        }
        series.push(serie);
      });
      const chartOptions = {
          tooltip : {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              label: {backgroundColor: '#6a7985'}
            }
          },
          toolbox: {
            feature: {saveAsImage: {}}
          },
          xAxis : [{
            type : 'category',
            boundaryGap : false,
            data : chartsData.labels,
          }],
          yAxis : [{type : 'value'}],
      };
      chartOptions.series = series;
      const chart = echarts.init($container[0]);
      chart.setOption(chartOptions);
    }
  }
}

</script>
