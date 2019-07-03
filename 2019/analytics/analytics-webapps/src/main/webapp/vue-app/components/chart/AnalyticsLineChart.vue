<script>
export default {
  extends: VueChart.Line,
  props: {
    chartTitle: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data () {
    return {
      gradient: null,
    }
  },
  watch: {
    data() {
      this.init();
    }
  },
  methods: {
    init(labels, data) {
      if (!data) {
        console.debug('No chart data', data);
        return;
      }
      if (!labels) {
        console.debug('No chart labels', labels);
        return;
      }
      if (!this.chartTitle) {
        console.debug('No chart label', this.chartTitle);
      }
      this.gradient = this.$refs.canvas.getContext('2d').createLinearGradient(0, 0, 0, 450)
      this.gradient.addColorStop(0, 'rgba(255, 0,0, 0.5)')
      this.gradient.addColorStop(0.5, 'rgba(255, 0, 0, 0.25)');
      this.gradient.addColorStop(1, 'rgba(255, 0, 0, 0)');

      this.renderChart({
        labels: labels,
        datasets: [
          {
            label: this.chartTitle || 'Chart title',
            borderColor: '#05CBE1',
            pointBackgroundColor: 'white',
            pointBorderColor: 'white',
            borderWidth: 1,
            backgroundColor: this.gradient,
            data: data,
          }
        ]
      }, {responsive: true, maintainAspectRatio: false});
    }
  }
}

</script>
