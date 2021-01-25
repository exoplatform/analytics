<template>
  <v-flex :id="id" class="analytics-chart-body" />
</template>
<script>
export default {
  props: {
    chartType: {
      type: String,
      default: function() {
        return 'line';
      },
    },
    title: {
      type: String,
      default: function() {
        return null;
      },
    },
    colors: {
      type: Array,
      default: function() {
        return [
          '#319ab3',
          '#f97575',
          '#98cc81',
          '#4273c8',
          '#cea6ac',
          '#bc99e7',
          '#9ee4f5',
          '#774ea9',
          '#ffa500',
          '#bed67e',
          '#bc99e7',
          '#ffaacc',
        ];
      },
    },
  },
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
    init(chartsData) {
      const charts = (chartsData && chartsData.charts) || [];
      const labels = (chartsData && chartsData.labels) || [];

      const $container = $(`#${this.id}`);
      if (!$container.length) {
        console.debug('No chart container for selector', this.id);
        return;
      }

      const series = [];
      const chartOptions = {
        title : {
          x: 'center'
        },
        grid: {
          top: 0,
          left: 30,
          bottom: 30,
        },
        color: this.colors,
        series : series,
      };

      if (this.chartType === 'line' || this.chartType === 'bar') {
        Object.assign(chartOptions, {
          grid: {show: false},
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              label: {backgroundColor: '#6a7985'}
            },
          },
          xAxis: [{
            type : 'category',
            showGrid: false,
            data : labels,
            splitLine: {
              show: false
            },
          }],
          yAxis: [{
            type : 'value',
            splitLine: {
              show: false
            },
          }],
        });
        if (this.chartType === 'line') {
          chartOptions.xAxis[0].boundaryGap = false;
        } else {
          chartOptions.xAxis[0].axisTick = {alignWithLabel: true};
        }
        if (charts.length === 1 && (!charts[0].chartLabel || charts[0].chartLabel === 'null')) {
          chartOptions.tooltip.formatter = '{b}<br/><center>{c}</center>';
        }
        charts.forEach(chartData => {
          const serie = {
            type: this.chartType,
            data: chartData.values,
          };

          if (chartData.chartLabel){
            serie.name = this.getI18N(chartData.chartLabel);
          }
          series.push(serie);
        });
      } else if (this.chartType === 'pie') {
        chartOptions.tooltip = {
          trigger: 'item',
          formatter: "{b} : {c} ({d}%)"
        };

        const chartsLength = charts.length;
        const chartsDividerLength = parseInt((chartsLength + 1) / 2) + 1;
        const chartsPercentagePart = parseInt(100 / chartsDividerLength);

        charts.forEach((chartData, index) => {
          const chartDataValues = chartData.aggregationResults.map(result => {
            const chartDataValues = {
              value: result.result,
            };
            if (result.label) {
              chartDataValues.name = this.getI18N(result.label);
            }
            return chartDataValues;
          });

          const xPos = parseInt((parseInt(index / (chartsDividerLength - 1)) + 1) * chartsPercentagePart) * 1.25;
          const yPos = parseInt((parseInt(index % (chartsDividerLength - 1)) + 1) * chartsPercentagePart);
          const serie = {
            type: this.chartType,
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

          serie.name = chartData.chartLabel;
          series.push(serie);
        });
      }

      const chart = echarts.init($container[0]);
      chart.setOption(chartOptions, true);
    },
    getI18N(label){
      const field = label.split("=")[1];
      const fieldLabelI18NKey = `analytics.${field}`;
      const fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
      return  fieldLabelI18NValue === fieldLabelI18NKey ? field : fieldLabelI18NValue;
    }
  }
}

</script>
