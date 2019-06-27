import './../css/main.less';

import Analytics from './components/Analytics.vue';

Vue.use(Vuetify);

const vueInstance = new Vue({
  el: '#AnalyticsApp',
  render: (h) => h(Analytics),
});
