import AnalyticsRateApplication from './components/AnalyticsRateApplication.vue';
import AnalyticsPercentageBarChart from './components/chart/AnalyticsPercentageBarChart.vue';

const components = {
  'analytics-rate-application': AnalyticsRateApplication,
  'analytics-percentage-bar-chart': AnalyticsPercentageBarChart,
};

for (const key in components) {
  Vue.component(key, components[key]);
}