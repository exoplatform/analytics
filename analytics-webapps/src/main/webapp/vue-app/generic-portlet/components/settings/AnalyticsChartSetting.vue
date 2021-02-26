<template>
  <v-dialog
    id="analyticsChartSettingsModal"
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="editChatSettings"
    width="750px"
    max-width="100vw"
    @keydown.esc="dialog = false">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="dialog = false">
        </a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle">
          {{ $t('analytics.editChart') }}
        </span>
      </div>
      <v-card-text>
        <v-tabs
          v-model="tab"
          background-color="transparent"
          color="primary">
          <v-tab>{{ $t('analytics.general') }}</v-tab>
          <v-tab>{{ $t('analytics.colors') }}</v-tab>
          <v-tab v-if="!isPercentageBar">{{ $t('analytics.xAxis') }}</v-tab>
          <v-tab>{{ $t('analytics.yAxis') }}</v-tab>
          <v-tab v-if="!isPercentageBar">{{ $t('analytics.multipleCharts') }}</v-tab>
          <v-tab>{{ $t('analytics.dataFilters') }}</v-tab>
        </v-tabs>
        <v-tabs-items v-model="tab">
          <v-tab-item eager>
            <analytics-general-setting-form
              ref="settingForm"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <analytics-colors-setting-form
              ref="colorsForm"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item v-if="!isPercentageBar" eager>
            <analytics-x-axis-form
              ref="xAxis"
              :fields-mappings="fieldsMappings"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <h3 v-if="isPercentageBar">chart value</h3>
            <analytics-y-axis-form
              ref="yAxis"
              :fields-mappings="fieldsMappings"
              :settings="chartSettings"
              :type="isPercentageBar ? 'value' : null" />
            <template v-if="isPercentageBar">
              <v-divider class="my-4" />
              <h3>chart threshold</h3>
              <analytics-y-axis-form
                ref="yAxis"
                :fields-mappings="fieldsMappings"
                :settings="chartSettings"
                :type="isPercentageBar ? 'threshold' : null" />
            </template>
          </v-tab-item>
          <v-tab-item v-if="!isPercentageBar" eager>
            <analytics-multiple-charts
              ref="multipleCharts"
              :fields-mappings="fieldsMappings"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <analytics-search-filter-form
              ref="searchFilter"
              :fields-mappings="fieldsMappings"
              :filters="isPercentageBar ? chartSettings.threshold.filters : chartSettings.filters" />
            <template v-if="isPercentageBar">
              <v-divider class="my-4" />
              <analytics-search-filter-form
                ref="searchFilter"
                :fields-mappings="fieldsMappings"
                :filters="chartSettings.value.filters" />
            </template>
          </v-tab-item>
        </v-tabs-items>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <button class="btn btn-primary ignore-vuetify-classes mr-1" @click="save">
          {{ $t('analytics.save') }}
        </button>
        <button class="btn ignore-vuetify-classes ml-1" @click="dialog = false">
          {{ $t('analytics.close') }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>

export default {
  props: {
    retrieveMappingsUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    settings: {
      type: Object,
      default: function() {
        return null;
      },
    },
    users: {
      type: Object,
      default: function() {
        return null;
      },
    },
    spaces: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      chartSettings: {},
      fieldsMappings: [],
      dialog: false,
      tab: 0,
    };
  },
  computed: {
    settingJsonContent() {
      return this.settings && JSON.stringify(this.settings, null, 2);
    },
    chartType() {
      return this.chartSettings && this.chartSettings.chartType;
    },
    isPercentageBar() {
      return this.chartType === 'percentageBar' || this.chartType=== 'percentage';
    },
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.init();
      }
    },
    isPercentageBar() {
      if (this.isPercentageBar) {
        this.chartSettings.multipleChartsField = null;
      } else {
        this.chartSettings.multipleChartsField = this.settings.multipleChartsField;
      }
    },
  },
  methods: {
    init() {
      this.loading = true;

      return fetch(this.retrieveMappingsUrl, {
        method: 'GET',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
        },
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error('Error getting analytics fields mappings:');
          }
        })
        .then((fieldsMappings) => {
          this.fieldsMappings = fieldsMappings;
          return this.$nextTick();
        })
        .then(() => {
          if (this.$refs) {
            Object.keys(this.$refs).forEach(refKey => {
              const component = this.$refs[refKey];
              if (component && component.init) {
                component.init();
              }
            });
          }
        })
        .catch((e) => {
          console.debug('fetch analytics - error', e);
          this.error = 'Error getting analytics';
        })
        .finally(() => this.loading = false);
    },
    open() {
      this.chartSettings = JSON.parse(JSON.stringify(this.settings));
      this.dialog = true;
    },
    save() {
      this.$emit('save', this.chartSettings);
      this.dialog = false;
    },
  },
};
</script>
