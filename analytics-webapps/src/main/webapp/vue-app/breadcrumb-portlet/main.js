import './initComponents.js';

Vue.use(Vuetify);
const vuetify = new Vuetify({
    dark: true,
    iconfont: '',
});

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'analyticsDashboardBreadcrumb';

// getting language of user
const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.Analytics-${lang}.json`;

export function init() {
    exoi18n.loadLanguageAsync(lang, url).then(i18n => {
        const appElement = document.createElement('div');
        appElement.id = appId;

        // init Vue app when locale ressources are ready
        new Vue({
            mounted() {
                document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
            },
            template: `<bread-crumb-analytics v-cacheable id="${appId}" />`,
            vuetify,
            i18n
        }).$mount(appElement);
    });
}