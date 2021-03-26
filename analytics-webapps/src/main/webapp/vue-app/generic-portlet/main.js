import './initComponents.js';

Vue.use(Vuetify);

const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

// getting language of user
const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Analytics-${lang}.json`;

// Display loading first time the page is displayed
document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

export function init(dataId) {
  // getting locale ressources
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      const element = $(`#${dataId}`);
      const retrieveSettingsURL = element.attr('data-settings-url');
      const retrieveMappingsURL = element.attr('data-mappings-url');
      const retrieveFiltersURL = element.attr('data-filters-url');
      const retrieveChartDataURL = element.attr('data-chart-data-url');
      const retrieveChartSamplesURL = element.attr('data-chart-samples-url');
      const saveSettingsURL = element.attr('data-save-settings-url');

      new Vue({
        data: () => ({
          retrieveSettingsURL : retrieveSettingsURL,
          retrieveMappingsURL : retrieveMappingsURL,
          retrieveFiltersURL : retrieveFiltersURL,
          retrieveChartDataURL : retrieveChartDataURL,
          retrieveChartSamplesURL : retrieveChartSamplesURL,
          saveSettingsURL : saveSettingsURL,
        }),
        mounted() {
          // Hide loading toolbar each time a portlet is mounted
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        },
        template: `
        <analytics-application
           id="${dataId}"
           :retrieve-settings-url="retrieveSettingsURL"
           :retrieve-mappings-url="retrieveMappingsURL"
           :retrieve-filters-url="retrieveFiltersURL"
           :retrieve-chart-data-url="retrieveChartDataURL"
           :retrieve-chart-samples-url="retrieveChartSamplesURL"
           :save-settings-url="saveSettingsURL"
           data-id="${dataId}"
           data-settings-url="${retrieveSettingsURL}"
           data-mappings-url="${retrieveMappingsURL}"
           data-filters-url="${retrieveFiltersURL}"
           data-chart-data-url="${retrieveChartDataURL}"
           data-chart-samples-url="${retrieveChartSamplesURL}"
           data-save-settings-url="${saveSettingsURL}" />`,
        vuetify,
        i18n
      }).$mount(`#${dataId}`);
    });
}
