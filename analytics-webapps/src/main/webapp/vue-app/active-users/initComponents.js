import AnalyticsActiveUsers from './components/AnalyticsActiveUsers.vue';
import AnalyticsPercentageBarChart from './components/chart/AnalyticsPercentageBarChart.vue';

const components = {
  'analytics-active-users': AnalyticsActiveUsers,
  'analytics-percentage-bar-chart': AnalyticsPercentageBarChart,
};

for (const key in components) {
  Vue.component(key, components[key]);
}