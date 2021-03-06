import AnalyticsChartSetting from './components/settings/AnalyticsChartSetting.vue';
import JsonPanelDialog from './components/settings/JsonPanelDialog.vue';

import IdentityFieldSelection from './components/settings/form/IdentityFieldSelection.vue';
import FieldSelection from './components/settings/form/FieldSelection.vue';
import FieldFilter from './components/settings/form/FieldFilter.vue';
import SpaceFieldFilter from './components/settings/form/SpaceFieldFilter.vue';
import UserFieldFilter from './components/settings/form/UserFieldFilter.vue';
import TextValuesFilter from './components/settings/form/TextValuesFilter.vue';
import TextValueSuggester from './components/settings/form/TextValueSuggester.vue';
import MultipleValuesSelection from './components/settings/form/MultipleValuesSelection.vue';
import XAxisAggregationField from './components/settings/form/XAxisAggregationField.vue';
import LimitFilterForm from './components/settings/form/LimitFilterForm.vue';

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

import SelectPeriod from './components/common/SelectPeriod.vue';

const components = {
  'analytics-profile-chip': ProfileChip,
  'analytics-sample-item': SampleItem,
  'analytics-setting-color-picker': SettingColorPicker,
  'analytics-identity-field-selection': IdentityFieldSelection,
  'analytics-field-selection': FieldSelection,
  'analytics-multiple-values-selection': MultipleValuesSelection,
  'analytics-space-field-filter': SpaceFieldFilter,
  'analytics-user-field-filter': UserFieldFilter,
  'analytics-text-value-filter': TextValuesFilter,
  'analytics-text-value-suggester': TextValueSuggester,
  'analytics-field-filter': FieldFilter,
  'analytics-x-axis-aggregation-field': XAxisAggregationField,
  'analytics-limit-filter-form': LimitFilterForm,
  'analytics-general-setting-form': GeneralSettingForm,
  'analytics-colors-setting-form': ColorsSettingForm,
  'analytics-search-filter-form': SearchFilterForm,
  'analytics-x-axis-form': XAxisForm,
  'analytics-y-axis-form': YAxisForm,
  'analytics-multiple-charts': MultipleCharts,
  'analytics-select-period': SelectPeriod,
  'analytics-chart-setting': AnalyticsChartSetting,
  'analytics-json-panel-dialog': JsonPanelDialog,
  'analytics-view-samples-drawer': ViewSamplesDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

import * as analyticsUtils from './js/utils.js';

if (!Vue.prototype.$analyticsUtils) {
  window.Object.defineProperty(Vue.prototype, '$analyticsUtils', {
    value: analyticsUtils,
  });
}
