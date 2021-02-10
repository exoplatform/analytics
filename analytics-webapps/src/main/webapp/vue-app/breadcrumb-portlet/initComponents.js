import Breadcrumb from './components/AnalyticsDashboardBreadcrumb.vue';
import AnalyticsDrawerNavigation from './components/AnalyticsDrawerNavigation.vue';

const components = {
  'analytics-breadcrumb': Breadcrumb,
  'analytics-drawer-navigation': AnalyticsDrawerNavigation,
};

for (const key in components) {
  Vue.component(key, components[key]);
}