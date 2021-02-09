import ActiveUsres from './components/ActiveUsers.vue';

const components = {
  'active-users': ActiveUsres,
};

for (const key in components) {
  Vue.component(key, components[key]);
}