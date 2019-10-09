<template>
  <v-dialog
    id="sendTokensModal"
    v-model="dialog"
    :attach="`#${parentId}`"
    content-class="uiPopup with-overflow"
    class="editChatSettings"
    width="700px"
    max-width="100vw"
    persistent
    @keydown.esc="dialog = false">
    <template v-slot:activator="{ on }">
      <v-btn
        text
        v-on="on">
        Edit
      </v-btn>
    </template>
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
            <x-axis-form ref="xAxis" :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <y-axis-form ref="yAxis" :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <multiple-charts ref="multipleCharts" :settings="chartSettings" />
          </v-tab-item>
          <v-tab-item eager>
            <search-filter-form ref="searchFilter" :filters="chartSettings.filters" />
          </v-tab-item>
        </v-tabs-items>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <button class="btn btn-primary ignore-vuetify-classes" @click="save">
          Save settings
        </button>
        <v-spacer />
        <button class="btn ignore-vuetify-classes" @click="dialog = false">
          Close
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import GeneralSettingForm from './tabs/GeneralSettingForm.vue';
import SearchFilterForm from './tabs/SearchFilterForm.vue';
import XAxisForm from './tabs/XAxisAggregationForm.vue';
import YAxisForm from './tabs/YAxisAggregationForm.vue';
import MultipleCharts from './tabs/MultipleChartsAggregationForm.vue';

export default {
  components: {
    GeneralSettingForm,
    SearchFilterForm,
    XAxisForm,
    YAxisForm,
    MultipleCharts,
  },
  props: {
    parentId: {
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
  },
  data() {
    return {
      dialog: false,
      tab: 0,
    };
  },
  computed: {
    chartSettings() {
      return Object.assign({}, this.settings);
    }
  },
  methods: {
    save() {
      this.$emit('save', this.chartSettings);
      this.dialog = false;
    }
  },
};
</script>
