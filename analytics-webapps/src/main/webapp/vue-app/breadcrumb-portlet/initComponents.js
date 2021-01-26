import Breadcrumb from './components/AnalyticsDashboardBreadcrumb.vue';

const components = {
    'bread-crumb-analytics': Breadcrumb,
};

for (const key in components) {
    Vue.component(key, components[key]);
}