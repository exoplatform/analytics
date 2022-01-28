import './initComponents.js';

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'analyticsDashboardBreadcrumb';

// getting language of user
const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Analytics-${lang}.json`;

export function init(cacheId) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;
    console.log('====== form main.js=====');
    console.log('====== so our cacheId should be ===== analyticsDashboardBreadcrumb/portal/dw/analytics ');
    console.log('====== so our appId should be ===== analyticsDashboardBreadcrumb ');
    console.log('this is the cacheId ',cacheId,' and this is the app element id : ',appId);
    // init Vue app when locale ressources are ready
    new Vue({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<analytics-breadcrumb v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" />`,
      vuetify,
      i18n
    }).$mount(appElement);
  });
}