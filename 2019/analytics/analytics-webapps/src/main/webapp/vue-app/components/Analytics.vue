<template>
  <v-app 
    :id="appId"
    class="analytics-application transparent"
    flat>
    <main>
      <v-card class="px-3 ma-auto transparent" flat>
        <v-card-title
          v-if="loading"
          primary-title
          class="ma-auto">
          <v-progress-circular
            color="primary"
            indeterminate
            size="20" />
        </v-card-title>

        <v-card-title v-else-if="chartsData" primary-title>
          <div>
            <h3 v-if="chartTitle" class="headline my-auto">{{ chartTitle }}</h3>
            <div v-if="chartSetting.displayComputingTime && chartsData.dataCount"> Total samples count {{ chartsData.dataCount }} </div>
            <div v-if="chartSetting.displaySamplesCount && chartsData.computingTime"> Computing time: {{ chartsData.computingTime }} ms</div>
          </div>
          <analytics-chart-setting
            v-if="chartSetting"
            :parent-id="modalParentId"
            :settings="chartSetting"
            class="mt-0"
            @save="saveSettings" />
        </v-card-title>

        <v-card-text class="px-0 mx-0">
          <analytics-chart ref="analyticsChart" />
        </v-card-text>
      </v-card>
      <div :id="modalParentId"></div>
    </main>
  </v-app>
</template>

<script>
import AnalyticsChart from './chart/AnalyticsChart.vue';
import AnalyticsChartSetting from './settings/AnalyticsChartSetting.vue';

export default {
  components: {
    AnalyticsChart,
    AnalyticsChartSetting,
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
    loading: true,
    appId: `AnalyticsApplication${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    modalParentId: `analyticsModals${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    chartsData: {},
    chartSetting: null,
  }),
  computed: {
    chartTitle() {
      return this.chartSetting && this.chartSetting.title;
    },
  },
  mounted() {
    this.init();
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
          this.chartSetting = preferences && preferences.settings && JSON.parse(preferences.settings);
          if (!this.chartSetting) {
            this.chartSetting = {
              filters: [],
              aggregations: [],
            };
          }
          if (!this.chartSetting.filters) {
            this.chartSetting.filters = [];
          }
          if (!this.chartSetting.xAxisAggregations) {
            this.chartSetting.xAxisAggregations = [];
          }
          if (!this.chartSetting.yAxisAggregation) {
            this.chartSetting.yAxisAggregation = {};
          }
        });
    },
    saveSettings(chartSetting) {
      this.loading = true;

      this.chartSetting = chartSetting;

      return fetch(this.saveSettingsURL, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: $.param({
          settings : JSON.stringify(this.chartSetting)
        }),
      })
        .then((resp) => {
          if (!resp || !resp.ok) {
            throw new Error('Error saving chart settings', this.chartSetting);
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
      this.loading = true;

      return fetch('/portal/rest/analytics', {
        method: 'POST',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          filters: this.chartSetting.filters,
          xAxisAggregations: this.chartSetting.xAxisAggregations,
          yAxisAggregation: this.chartSetting.yAxisAggregation,
          multipleChartsField: this.chartSetting.multipleChartsField,
          offset: this.chartSetting.offset,
          limit: this.chartSetting.limit,
          lang: eXo.env.portal.language,
        }),
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error('Error getting analytics with settings:', this.chartSetting);
          }
        })
        .then((chartsData) => {
          this.chartsData = chartsData;
          this.$refs.analyticsChart.init(this.chartsData, this.chartSetting);
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