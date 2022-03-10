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
  
    // init Vue app when locale ressources are ready
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<analytics-breadcrumb v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" />`,
      vuetify,
      i18n
     
    }, `#${appId}`, 'analytics');
  });
}