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
          Edit chart settings
        </span>
      </div>
      <v-card-text>
        <v-tabs
          v-model="tab"
          background-color="transparent"
          color="primary">
          <v-tab>General</v-tab>
          <v-tab>Colors</v-tab>
          <v-tab>X axis</v-tab>
          <v-tab>Y axis</v-tab>
          <v-tab>Multiple charts</v-tab>
          <v-tab>Data filters</v-tab>
        </v-tabs>
        <v-tabs-items v-model="tab">
          <v-tab-item eager>
            <general-setting-form
              ref="settingForm"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <colors-setting-form
              ref="colorsForm"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <x-axis-form
              ref="xAxis"
              :fields-mappings="fieldsMappings"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <y-axis-form
              ref="yAxis"
              :fields-mappings="fieldsMappings"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <multiple-charts
              ref="multipleCharts"
              :fields-mappings="fieldsMappings"
              :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <search-filter-form
              ref="searchFilter"
              :fields-mappings="fieldsMappings"
              :filters="chartSettings.filters" />
          </v-tab-item>
        </v-tabs-items>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <button class="btn btn-primary ignore-vuetify-classes mr-1" @click="save">
          Save settings
        </button>
        <button class="btn ignore-vuetify-classes ml-1" @click="dialog = false">
          Close
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import GeneralSettingForm from './tabs/GeneralSettingForm.vue';
import ColorsSettingForm from './tabs/ColorsSettingForm.vue';
import SearchFilterForm from './tabs/SearchFilterForm.vue';
import XAxisForm from './tabs/XAxisAggregationForm.vue';
import YAxisForm from './tabs/YAxisAggregationForm.vue';
import MultipleCharts from './tabs/MultipleChartsAggregationForm.vue';

export default {
  components: {
    GeneralSettingForm,
    ColorsSettingForm,
    SearchFilterForm,
    XAxisForm,
    YAxisForm,
    MultipleCharts,
  },
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
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.init();
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
    }
  },
};
</script>
