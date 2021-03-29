import AnalyticsRateApplication from './components/AnalyticsRateApplication.vue';
import AnalyticsPercentageBarChart from './components/chart/AnalyticsPercentageBarChart.vue';
import AnalyticsPercentageChart from './components/chart/AnalyticsPercentageChart.vue';

const components = {
  'analytics-rate-application': AnalyticsRateApplication,
  'analytics-percentage-bar-chart': AnalyticsPercentageBarChart,
  'analytics-percentage-chart': AnalyticsPercentageChart,
};

for (const key in components) {
  Vue.component(key, components[key]);
}