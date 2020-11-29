<template>
  <v-app 
    :id="appId"
    class="analytics-application"
    flat>
    <main>
      <analytics-chart-setting
        v-if="chartSettings"
        ref="chartSettingDialog"
        :retrieve-mappings-url="retrieveMappingsURL"
        :settings="chartSettings"
        :users="userObjects"
        :spaces="spaceObjects"
        class="mt-0"
        @save="saveSettings" />
      <json-panel-dialog
        v-if="chartSettings"
        ref="jsonPanelDialog"
        :settings="chartSettings"
        class="mt-0" />
      <view-samples-drawer
        ref="viewSamplesDrawer"
        :title="title"
        :selected-period="selectedPeriod"
        :users="userObjects"
        :spaces="spaceObjects"
        :retrieve-samples-url="retrieveChartSamplesURL"
        class="mt-0" />
      <v-card class="mx-3 mt-4 ma-auto white" flat>
        <v-toolbar flat>
          <v-toolbar-title :title="title">{{ title }}</v-toolbar-title>
          <v-spacer />
          <v-toolbar-title :title="scopeTooltip">
            <v-chip :color="scopeColor" :text-color="scopeTextColor">{{ scopeTitle }}</v-chip>
          </v-toolbar-title>
          <v-spacer />
          <select-period v-model="selectedPeriod" />
          <v-menu offset-y>
            <template v-slot:activator="{ on }">
              <v-btn
                icon
                class="ml-2"
                v-on="on">
                <v-icon>mdi-dots-vertical</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item v-if="chartSettings" @click="$refs.chartSettingDialog.open()">
                <v-list-item-title>Settings</v-list-item-title>
              </v-list-item>
              <v-list-item v-if="chartSettings" @click="$refs.jsonPanelDialog.open()">
                <v-list-item-title>View JSON panel</v-list-item-title>
              </v-list-item>
              <v-list-item @click="$refs.viewSamplesDrawer.open()">
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
            :title="title"
            :chart-type="chartType" />
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
    retrieveMappingsURL: {
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
    error: null,
    scope: null,
    title: null,
    chartType: 'line',
    displayComputingTime: false,
    displaySamplesCount: false,
    selectedPeriod: null,
    userObjects: {},
    spaceObjects: {},
    loading: true,
    appId: `AnalyticsApplication${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    chartsData: {},
    chartSettings: null,
  }),
  computed: {
    scopeColor() {
      switch (this.scope) {
      case 'NONE': return 'grey';
      case 'GLOBAL': return 'purple';
      case 'USER': return 'green';
      case 'SPACE': return 'blue';
      }
      return this.error && 'red';
    },
    scopeTextColor() {
      return `${this.scopeColor} lighten-5`;
    },
    scopeTitle() {
      switch (this.scope) {
      case 'NONE': return 'Permission denied';
      case 'GLOBAL': return 'ALL Data';
      case 'USER': return 'User data';
      case 'SPACE': return 'Space data';
      }
      return this.error;
    },
    scopeTooltip() {
      switch (this.scope) {
      case 'NONE': return 'Permission denied';
      case 'GLOBAL': return 'No data restriction';
      case 'USER': return 'Data restriction: current user data only';
      case 'SPACE': return 'Data restriction: current space data only';
      }
      return null;
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
              .then(this.getFilters)
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
            throw new Error(`Error getting analytics of chart '${this.title}'`);
          }
        })
        .then((settings) => {
          this.scope = settings && settings.scope;
          this.canEdit = settings && settings.canEdit;
          this.chartType = settings && settings.chartType;
          this.title = settings && settings.title;
          this.displayComputingTime = settings && settings.displayComputingTime;
          this.displaySamplesCount = settings && settings.displaySamplesCount;
        })
        .catch((e) => {
          console.warn('Error retrieving chart filters', e);
          this.error = 'Error retrieving chart filters';
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
        .then((settings) => {
          this.chartSettings = settings;
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
        })
        .catch((e) => {
          console.warn('Error retrieving chart filters', e);
          this.error = 'Error retrieving chart filters';
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
          // Wait until portlet preferences store transaction gets really saved
          // and gets available. (To DELETE once Portal RDBMS is merged) 
          window.setTimeout(this.init, 100);
        })
        .catch((e) => {
          console.warn('Error saving chart settings', e);
          this.error = 'Error saving chart settings';
          this.loading = false;
        });
    },
    updateChart() {
      if (!this.selectedPeriod) {
        return;
      }

      this.loading = true;
      const params = {
        lang: eXo.env.portal.language,
        min: this.selectedPeriod.min,
        max: this.selectedPeriod.max,
      };
      return fetch(this.retrieveChartDataURL, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: $.param(params),
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