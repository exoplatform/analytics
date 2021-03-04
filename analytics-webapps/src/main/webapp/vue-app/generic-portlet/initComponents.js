import AnalyticsApplication from './components/AnalyticsApplication.vue';

import AnalyticsChart from './components/chart/AnalyticsChart.vue';

const components = {
  'analytics-application': AnalyticsApplication,
  'analytics-chart': AnalyticsChart,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
