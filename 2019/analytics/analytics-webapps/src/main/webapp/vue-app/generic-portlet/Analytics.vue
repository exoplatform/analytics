<template>
  <v-app 
    :id="appId"
    class="analytics-application transparent"
    flat>
    <main>
      <analytics-chart-setting
        v-if="chartSettings"
        ref="chartSettingDialog"
        :parent-id="modalParentId"
        :settings="chartSettings"
        :users="userObjects"
        :spaces="spaceObjects"
        class="mt-0"
        @save="saveSettings" />
      <json-panel-dialog
        v-if="chartSettings"
        ref="jsonPanelDialog"
        :parent-id="modalParentId"
        :settings="chartSettings"
        class="mt-0" />
      <view-samples-drawer
        v-if="chartSettings"
        ref="viewSamplesDrawer"
        :parent-id="modalParentId"
        :settings="chartSettings"
        :selected-period="selectedPeriod"
        :users="userObjects"
        :spaces="spaceObjects"
        class="mt-0" />
      <v-card class="px-3 pt-4 ma-auto transparent" flat>
        <v-toolbar
          color="white"
          class="elevation-1">
          <v-toolbar-title :title="chartTitle">{{ chartTitle }}</v-toolbar-title>
          <v-spacer />
          <select-period v-model="selectedPeriod" />
          <v-menu open-on-hover>
            <template v-slot:activator="{ on }">
              <v-btn icon v-on="on">
                <v-icon>mdi-dots-vertical</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item v-if="$refs.chartSettingDialog" @click="$refs.chartSettingDialog.open()">
                <v-list-item-title>Settings</v-list-item-title>
              </v-list-item>
              <v-list-item v-if="$refs.jsonPanelDialog" @click="$refs.jsonPanelDialog.open()">
                <v-list-item-title>View JSON panel</v-list-item-title>
              </v-list-item>
              <v-list-item v-if="$refs.viewSamplesDrawer" @click="$refs.viewSamplesDrawer.open()">
                <v-list-item-title>View samples</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </v-toolbar>

        <v-card-title
          v-if="loading"
          primary-title
          class="ma-auto">
          <v-spacer />
          <v-progress-circular
            color="primary"
            indeterminate
            size="20" />
          <v-spacer />
        </v-card-title>

        <v-card-text class="px-0 mx-0">
          <analytics-chart
            ref="analyticsChart"
            :settings="chartSettings" />
        </v-card-text>

        <div v-if="displayComputingTime || displaySamplesCount" class="pl-4">
          <div v-if="displayComputingTime" class="subtitle-1">
            Total samples count {{ chartsData.dataCount }}
          </div>
          <div v-if="displaySamplesCount" class="subtitle-1">
            Computing time: {{ chartsData.computingTime }} ms
          </div>
        </div>
      </v-card>
      <div :id="modalParentId"></div>
    </main>
  </v-app>
</template>

<script>
import AnalyticsChart from './chart/AnalyticsChart.vue';
import SelectPeriod from './chart/SelectPeriod.vue';
import AnalyticsChartSetting from './settings/AnalyticsChartSetting.vue';
import JsonPanelDialog from './settings/JsonPanelDialog.vue';
import ViewSamplesDrawer from './samples/ViewSamplesDrawer.vue';

export default {
  components: {
    AnalyticsChart,
    SelectPeriod,
    AnalyticsChartSetting,
    JsonPanelDialog,
    ViewSamplesDrawer,
  },
  props: {
    retrieveSettingsURL: {
      type: String,
      default: function() {
        return null;
      },
    },
    saveSettingsURL: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    selectedPeriod: null,
    userObjects: {},
    spaceObjects: {},
    loading: true,
    appId: `AnalyticsApplication${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    modalParentId: `analyticsModals${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    chartsData: {},
    chartSettings: null,
  }),
  computed: {
    chartTitle() {
      return this.chartSettings && this.chartSettings.title;
    },
    displayComputingTime() {
      return this.chartSettings && this.chartsData && this.chartSettings.displayComputingTime;
    },
    displaySamplesCount() {
      return this.chartSettings && this.chartsData && this.chartSettings.displaySamplesCount;
    },
  },
  watch: {
    selectedPeriod(newValue, oldValue) {
      if (!oldValue && newValue) {
        this.init();
      } else if (newValue) {
        this.updateChart();
      }
    },
  },
  methods: {
    init() {
      this.loading = true;
      return this.getSettings()
              .then(this.updateChart)
              .finally(() => {
                this.loading = false;
              });
    },
    getSettings() {
      return fetch(this.retrieveSettingsURL, {
        method: 'GET',
        credentials: 'include',
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error(`Error getting analytics of ${JSON.stringify(this.settings)}`);
          }
        })
        .then((preferences) => {
          try {
            this.chartSettings = preferences && preferences.settings && JSON.parse(preferences.settings);
          } catch(e) {
            console.debug('Error parsing setting', preferences);
            this.chartSettings = {};
          }
          if (!this.chartSettings) {
            this.chartSettings = {
              filters: [],
              aggregations: [],
            };
          }
          if (!this.chartSettings.filters) {
            this.chartSettings.filters = [];
          }
          if (!this.chartSettings.xAxisAggregations) {
            this.chartSettings.xAxisAggregations = [];
          }
          if (!this.chartSettings.yAxisAggregation) {
            this.chartSettings.yAxisAggregation = {};
          }
        });
    },
    saveSettings(chartSettings) {
      this.loading = true;

      this.chartSettings = chartSettings;

      return fetch(this.saveSettingsURL, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: $.param({
          settings : JSON.stringify(this.chartSettings)
        }),
      })
        .then((resp) => {
          if (!resp || !resp.ok) {
            throw new Error('Error saving chart settings', this.chartSettings);
          }
          return this.updateChart();
        })
        .catch((e) => {
          console.warn('Error saving chart settings', e);
          this.error = 'Error saving chart settings';
        })
        .finally(() => this.loading = false);
    },
    updateChart() {
      if (!this.chartSettings) {
        return;
      }
      if (!this.selectedPeriod) {
        return;
      }

      this.loading = true;

      const filters = this.chartSettings.filters.slice();
      if (this.selectedPeriod) {
        filters.push({
          field: "timestamp",
          type: "RANGE",
          range: this.selectedPeriod,
        });
      }

      return fetch('/portal/rest/analytics', {
        method: 'POST',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          filters: filters,
          xAxisAggregations: this.chartSettings.xAxisAggregations,
          yAxisAggregation: this.chartSettings.yAxisAggregation,
          multipleChartsField: this.chartSettings.multipleChartsField,
          offset: this.chartSettings.offset,
          limit: this.chartSettings.limit,
          lang: eXo.env.portal.language,
        }),
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error('Error getting analytics with settings:', this.chartSettings);
          }
        })
        .then((chartsData) => {
          this.chartsData = chartsData;
          this.$refs.analyticsChart.init(this.chartsData);
        })
        .catch((e) => {
          console.debug('fetch analytics - error', e);
          this.error = 'Error getting analytics';
        })
        .finally(() => this.loading = false);
    },
  }
};
</script>