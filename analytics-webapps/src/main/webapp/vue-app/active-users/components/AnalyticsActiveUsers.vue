<template>
  <v-app id="chartAnalyticsActiveUsers">
    <analytics-chart-setting
      ref="chartSettingDialog"
      :retrieve-mappings-url="retrieveMappingsUrl"
      :settings="chartSettings"
      :users="userObjects"
      :spaces="spaceObjects"
      class="mt-0"
      @save="saveSettings" />
    <v-card class="analytics-chart-parent white" flat>
      <div class="d-flex pa-3 analytics-chart-header" flat>
        <v-toolbar-title class="d-flex">
          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-btn
                height="20"
                width="20"
                icon
                small
                class="my-auto mr-2 primary"
                outlined
                v-bind="attrs"
                v-on="on">
                <v-icon size="12">fa-info</v-icon>
              </v-btn>
            </template>
            <span>
              <div>{{ $t('analytics.activeUsersInfo') }}</div>
            </span>
          </v-tooltip>
          <div
            :title="$t('analytics.activeUsers')"
            class="my-auto text-truncate analytics-chart-title">
            {{ $t('analytics.activeUsers') }}
          </div>
        </v-toolbar-title>
        <v-spacer />
        <analytics-select-period v-model="selectedPeriod" />
        <v-menu
          v-model="showMenu"
          offset-y>
          <template v-slot:activator="{ on }">
            <v-btn
              icon
              class="ml-2"
              v-on="on"
              @blur="closeMenu()">
              <v-icon>mdi-dots-vertical</v-icon>
            </v-btn>
          </template>
          <v-list>
            <v-list-item @mousedown="$event.preventDefault()" @click="$refs.chartSettingDialog.open()">
              <v-list-item-title>{{ $t('analytics.settings') }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
      <analytics-percentage-bar-chart
        ref="analyticsChartBody"
        :colors="colors" />
    </v-card>
  </v-app>
</template>

<script>
export default {
  props: {
    retrieveSettingsUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveMappingsUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveFiltersUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveChartDataUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveChartSamplesUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    saveSettingsUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    canEdit: false,
    value: 0,
    error: null,
    scope: null,
    title: null,
    chartType: 'line',
    initialized: false,
    showMenu:false,
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
    DEFAULT_COLORS: [
      '#319ab3',
      '#f97575',
      '#98cc81',
      '#4273c8',
      '#cea6ac',
      '#bc99e7',
      '#9ee4f5',
      '#774ea9',
      '#ffa500',
      '#bed67e',
      '#bc99e7',
      '#ffaacc',
    ],
  }),
  computed: {
    scopeTooltip() {
      switch (this.scope) {
      case 'NONE': return this.$t('analytics.permissionDenied');
      case 'GLOBAL': return this.$t('analytics.noDataRestriction');
      case 'USER': return this.$t('analytics.dataRestrictedToCurrentUser');
      case 'SPACE': return this.$t('analytics.dataRestrictedToCurrentSpace');
      }
      return this.error;
    },
    colors() {
      return this.chartSettings
          && this.chartSettings.colors
          && this.chartSettings.colors.length
          && this.chartSettings.colors.slice()
          || this.DEFAULT_COLORS;
    },
  },
  watch: {
    selectedPeriod(newValue, oldValue) {
      if (!oldValue && newValue && !this.initialized) {
        this.initialized = true;
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
        .then(this.$nextTick)
        .then(this.updateChart)
        .then(this.$nextTick)
        .then(this.getFilters)
        .finally(() => {
          this.loading = false;
        });
    },
    getSettings() {
      return fetch(this.retrieveSettingsUrl, {
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
          if (!this.chartSettings) {
            this.chartSettings = settings;
          }
          this.scope = settings && settings.scope;
          this.canEdit = settings && settings.canEdit;
          this.chartType = settings && settings.chartType;
          this.title = settings && settings.title || this.$t('analytics.chartDataPlaceholder');
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
      return fetch(this.retrieveFiltersUrl, {
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
          if (!settings) {
            this.chartSettings = {
              filters: [],
              aggregations: [],
            };
          }
          if (!this.chartSettings.threshold.filters) {
            this.chartSettings.threshold.filters = [];
          }
          if (!this.chartSettings.value.filters) {
            this.chartSettings.value.filters = [];
          }
          if (!this.chartSettings.xAxisAggregations) {
            this.chartSettings.xAxisAggregations = [];
          }
          if (!this.chartSettings.threshold.yAxisAggregation) {
            this.chartSettings.threshold.yAxisAggregation = {};
          }
          if (!this.chartSettings.value.yAxisAggregation) {
            this.chartSettings.value.yAxisAggregation = {};
          }
        })
        .catch((e) => {
          console.warn('Error retrieving chart filters', e);
          this.error = 'Error retrieving chart filters';
        });
    },
    saveSettings(chartSettings) {
      this.loading = true;

      return fetch(this.saveSettingsUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: $.param({
          settings : JSON.stringify(chartSettings)
        }),
      })
        .then((resp) => {
          if (!resp || !resp.ok) {
            throw new Error('Error saving chart settings', chartSettings);
          }

          this.chartSettings = JSON.parse(JSON.stringify(chartSettings));
          return this.init();
        })
        .catch((e) => {
          console.warn('Error saving chart settings', e);
          this.error = 'Error saving chart settings';
        })
        .finally(() => {
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
      return fetch(this.retrieveChartDataUrl, {
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
          // FIXME wrong data
          this.$refs.analyticsChartBody.init({
            'charts':[
              {
                'aggregationResults':[
                  {
                    'value':'2.0',
                    'result':'2.0',
                    'label':'W6 2021'
                  },
                  {
                    'value':'3.0',
                    'result':'3.0',
                    'label':'W7 2021'
                  }
                ],
              },
              {
                'aggregationResults':[
                  {
                    'value':'5.0',
                    'result':'5.0',
                    'label':'W6 2021'
                  },
                  {
                    'value':'10.0',
                    'result':'10.0',
                    'label':'W7 2021'
                  }
                ],
              }
            ],
          });

        })
        .catch((e) => {
          console.debug('fetch analytics - error', e);
          this.error = 'Error getting analytics';
        })
        .finally(() => this.loading = false);
    },
    closeMenu(){
      this.showMenu=false;
    }
  }
};
</script>