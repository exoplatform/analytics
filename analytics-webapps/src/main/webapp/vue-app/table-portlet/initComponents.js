import AnalyticsTableApplication from './components/AnalyticsTableApplication.vue';
import AnalyticsTableSetting from './components/settings/AnalyticsTableSetting.vue';
import AnalyticsTableColumnSetting from './components/settings/AnalyticsTableColumnSetting.vue';
import AnalyticsTableGeneralSetting from './components/settings/AnalyticsTableGeneralSetting.vue';
import AnalyticsTableCell from './components/table/AnalyticsTableCell.vue';
import AnalyticsTable from './components/table/AnalyticsTable.vue';

const components = {
  'analytics-table-application': AnalyticsTableApplication,
  'analytics-table-column-setting': AnalyticsTableColumnSetting,
  'analytics-table-general-setting': AnalyticsTableGeneralSetting,
  'analytics-table-setting': AnalyticsTableSetting,
  'analytics-table-cell': AnalyticsTableCell,
  'analytics-table': AnalyticsTable,
};

for (const key in components) {
  Vue.component(key, components[key]);
}