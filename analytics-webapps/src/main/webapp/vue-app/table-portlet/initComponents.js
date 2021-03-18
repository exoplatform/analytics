import AnalyticsTableApplication from './components/AnalyticsTableApplication.vue';
import AnalyticsUser from './components/tables/AnalyticsUser.vue';

const components = {
  'analytics-table-application': AnalyticsTableApplication,
  'analytics-user-application': AnalyticsUser,
};

for (const key in components) {
  Vue.component(key, components[key]);
}