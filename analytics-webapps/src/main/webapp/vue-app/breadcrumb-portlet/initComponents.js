import Breadcrumb from './components/AnalyticsDashboardBreadcrumb.vue';

const components = {
    'analytics-breadcrumb': Breadcrumb,
};

for (const key in components) {
    Vue.component(key, components[key]);
}