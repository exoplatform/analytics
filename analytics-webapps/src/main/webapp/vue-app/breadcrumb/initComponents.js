import Breadcrumb from './Breadcrumb.vue';

const components = {
    'bread-crumb': Breadcrumb,
};

for (const key in components) {
    Vue.component(key, components[key]);
}