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
      const charts = (chartsData && chartsData.charts) || [];
      const labels = (chartsData && chartsData.labels) || [];
      const chartType = settings && settings.chartType || 'line';

      const $container = $(`#${this.id}`);
      if (!$container.length) {
        console.debug('No chart container for selector', this.id);
        return;
      }

      const series = [];
      const chartOptions = {
          title : {
            text: settings.chartTitle,
            x:'center'
          },
          series : series,
      };

      if (chartType === 'line' || chartType === 'bar') {
        chartOptions.tooltip = {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {backgroundColor: '#6a7985'}
          }
        };
        chartOptions.xAxis = [{
          type : 'category',
          data : labels,
        }];
        if (chartType === 'line') {
          chartOptions.xAxis[0].boundaryGap = false;
        } else {
          chartOptions.xAxis[0].axisTick = {alignWithLabel: true};
        }

        chartOptions.yAxis = [{type : 'value'}];
        charts.forEach(chartData => {
          const serie = {
              type: chartType,
              data: chartData.values,
          };
          if (chartData.chartValue) {
            chartData.chartKey = chartData.chartKey.replace('.keyword', '');
            serie.name = `${chartData.chartKey}=${chartData.chartValue}`;
          } else if (chartData.key && chartData.key.fieldValue) {
            serie.name = chartData.key.fieldValue;
          }
          series.push(serie);
        });
      } else if (chartType === 'pie') {
        chartOptions.tooltip = {
          trigger: 'item',
          formatter: "{b} : {c} ({d}%)"
        };
        chartOptions.legend = {
          orient: 'vertical',
          left: 'left',
          data: labels,
        };

        const chartsLength = charts.length;
        const chartsDividerLength = parseInt((chartsLength + 1) / 2) + 1;
        const chartsPercentagePart = parseInt(100 / chartsDividerLength);

        charts.forEach((chartData, index) => {
          const chartDataValues = chartData.aggregationResults.map(result => {
            return {
              name: result.label,
              value: result.result
            };
          });

          const xPos = parseInt((parseInt(index / (chartsDividerLength - 1)) + 1) * chartsPercentagePart) * 1.25;
          const yPos = parseInt((parseInt(index % (chartsDividerLength - 1)) + 1) * chartsPercentagePart);
          const serie = {
            type: chartType,
            radius : `${chartsPercentagePart + 5}%`,
            center: [`${xPos}%`, `${yPos}%`],
            data: chartDataValues,
            itemStyle: {
              emphasis: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          };

          // margin legend
          if (chartsLength === 1) {
            serie.radius = '70%';
            serie.center = ['50%', '50%'];
          }

          if (chartData.chartValue) {
            chartData.chartKey = chartData.chartKey.replace('.keyword', '');
            serie.name = `${chartData.chartKey}=${chartData.chartValue}`;
          } else if (chartData.key && chartData.key.fieldValue) {
            serie.name = chartData.key.fieldValue;
          }
          series.push(serie);
        });
      }

      const chart = echarts.init($container[0]);
      chart.setOption(chartOptions, true);
    }
  }
}

</script>
