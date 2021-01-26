import Breadcrumb from './components/BreadcrumbAnalytics.vue';

const components = {
    'bread-crumb-analytics': Breadcrumb,
};

for (const key in components) {
    Vue.component(key, components[key]);
}