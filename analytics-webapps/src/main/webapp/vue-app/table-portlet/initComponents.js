import AnalyticsTableApplication from './components/AnalyticsTableApplication.vue';
import AnalyticsTable from './components/table/AnalyticsTable.vue';

const components = {
  'analytics-table-application': AnalyticsTableApplication,
  'analytics-table': AnalyticsTable,
};

for (const key in components) {
  Vue.component(key, components[key]);
}