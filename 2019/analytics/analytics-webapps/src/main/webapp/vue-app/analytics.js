import Vue from 'vue';
import Vuetify from 'vuetify'

import Analytics from './components/Analytics.vue';

import '../css/main.less';

Vue.use(Vuetify);

//To load all analytics applications in the same page only once
if (!window.loadingAnalytics) {
  window.loadingAnalytics = true;

  const vuetify = new Vuetify({
    dark: true,
    iconfont: '',
  });

  // getting language of user
  const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
  const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.Analytics-${lang}.json`;

  // getting locale ressources
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      $('.analytics-app-parent').each((index, elem) => {
        const dataId = $(elem).attr('data-id');
        const retrieveSettingsURL = $(elem).attr('data-settings-url');
        const saveSettingsURL = $(elem).attr('data-save-settings-url');
        // init Vue app when locale ressources are ready
        new Vue({
          render: h => h(Analytics, {
            props:{
              retrieveSettingsURL : retrieveSettingsURL,
              saveSettingsURL : saveSettingsURL,
            }
          }),
          i18n,
          vuetify,
        }).$mount(`[data-id="${dataId}"]`);
      });
    });
}
