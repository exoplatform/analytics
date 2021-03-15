import AnalyticsBaseApplication from './components/AnalyticsBaseApplication.vue';
import AnalyticsTable from './components/tables/AnalyticsTable.vue';

const components = {
  'analytics-base-application': AnalyticsBaseApplication,
  'analytics-table-application': AnalyticsTable,
};

for (const key in components) {
  Vue.component(key, components[key]);
}