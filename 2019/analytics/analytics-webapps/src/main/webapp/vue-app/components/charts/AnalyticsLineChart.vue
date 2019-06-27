<script>
export default {
  extends: VueChart.Line,
  props: {
    data: {
      type: Array,
      default: function() {
        return null;
      },
    },
    chartLabel: {
      type: Array,
      default: function() {
        return null;
      },
    },
    labels: {
      type: Array,
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
    init() {
      if (!this.data) {
        console.debug('No chart data', this.data);
        return;
      }
      if (!this.labels) {
        console.debug('No chart labels', this.labels);
        return;
      }
      if (!this.chartLabel) {
        console.debug('No chart label', this.chartLabel);
        return;
      }
      this.gradient = this.$refs.canvas.getContext('2d').createLinearGradient(0, 0, 0, 450)
      this.gradient.addColorStop(0, 'rgba(255, 0,0, 0.5)')
      this.gradient.addColorStop(0.5, 'rgba(255, 0, 0, 0.25)');
      this.gradient.addColorStop(1, 'rgba(255, 0, 0, 0)');

      this.renderChart({
        labels: this.labels,
        datasets: [
          {
            label: this.chartLabel,
            borderColor: '#05CBE1',
            pointBackgroundColor: 'white',
            pointBorderColor: 'white',
            borderWidth: 1,
            backgroundColor: this.gradient,
            data: this.data,
          }
        ]
      }, {responsive: true, maintainAspectRatio: false});
    }
  }
}

</script>
