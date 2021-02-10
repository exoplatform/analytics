import AnalyticsApplication from './components/AnalyticsApplication.vue';

import AnalyticsChartSetting from './components/settings/AnalyticsChartSetting.vue';
import JsonPanelDialog from './components/settings/JsonPanelDialog.vue';
import FieldSelection from './components/settings/form/FieldSelection.vue';
import SettingColorPicker from './components/settings/tabs/SettingColorPicker.vue';
import GeneralSettingForm from './components/settings/tabs/GeneralSettingForm.vue';
import ColorsSettingForm from './components/settings/tabs/ColorsSettingForm.vue';
import SearchFilterForm from './components/settings/tabs/SearchFilterForm.vue';
import XAxisForm from './components/settings/tabs/XAxisAggregationForm.vue';
import YAxisForm from './components/settings/tabs/YAxisAggregationForm.vue';
import MultipleCharts from './components/settings/tabs/MultipleChartsAggregationForm.vue';

import ViewSamplesDrawer from './components/samples/ViewSamplesDrawer.vue';
import ProfileChip from './components/samples/ProfileChip.vue';
import SampleItem from './components/samples/SampleItem.vue';

import AnalyticsChart from './components/chart/AnalyticsChart.vue';
import SelectPeriod from './components/chart/SelectPeriod.vue';

const components = {
  'analytics-application': AnalyticsApplication,
  'analytics-profile-chip': ProfileChip,
  'analytics-sample-item': SampleItem,
  'analytics-setting-color-picker': SettingColorPicker,
  'analytics-field-selection': FieldSelection,
  'analytics-general-setting-form': GeneralSettingForm,
  'analytics-colors-setting-form': ColorsSettingForm,
  'analytics-search-filter-form': SearchFilterForm,
  'analytics-x-axis-form': XAxisForm,
  'analytics-y-axis-form': YAxisForm,
  'analytics-multiple-charts': MultipleCharts,
  'analytics-chart': AnalyticsChart,
  'analytics-select-period': SelectPeriod,
  'analytics-chart-setting': AnalyticsChartSetting,
  'analytics-json-panel-dialog': JsonPanelDialog,
  'analytics-view-samples-drawer': ViewSamplesDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

import * as analyticsUtils from '../js/utils.js';

if (!Vue.prototype.$analyticsUtils) {
  window.Object.defineProperty(Vue.prototype, '$analyticsUtils', {
    value: analyticsUtils,
  });
}
