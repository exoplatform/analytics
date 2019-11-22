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
        :retrieve-samples-url="retrieveChartSamplesURL"
        class="mt-0" />
      <v-card class="px-3 pt-4 ma-auto transparent" flat>
        <v-toolbar
          color="white"
          class="elevation-1">
          <v-toolbar-title :title="chartTitle">{{ chartTitle }} - Scope: {{ scope }}</v-toolbar-title>
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
    retrieveFiltersURL: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveChartDataURL: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveChartSamplesURL: {
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
    canEdit: false,
    scope: 'NONE',
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
        .then((settings) => {
          this.canEdit = settings && settings.canEdit;
          this.scope = settings && settings.scope;
        });
    },
    getFilters() {
      if (!this.canEdit) {
        return;
      }
      return fetch(this.retrieveFiltersURL, {
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
      if (!this.selectedPeriod) {
        return;
      }

      this.loading = true;
      const params = {
        lang: eXo.env.portal.language,
      };
      if (this.selectedPeriod) {
        params.min = this.selectedPeriod.min;
        params.max = this.selectedPeriod.max;
      }
      return fetch(this.retrieveChartDataURL, {
        method: 'GET',
        credentials: 'include',
        body: $.params(params),
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