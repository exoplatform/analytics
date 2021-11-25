<template>
  <v-dialog
    id="jsonChartSettingsModal"
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="editChatSettings"
    width="750px"
    max-width="100vw"
    min-height="450px"
    max-height="100%"
    persistent
    @keydown.esc="dialog = false">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="dialog = false">
        </a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle">
          JSON Panel of chart <span class="primary--text">{{ settings && $t(settings.title) }}</span>
        </span>
      </div>
      <v-card-text>
        <v-textarea
          v-model="settingJsonContent"
          :row-height="15"
          class="jsonSetting"
          readonly />
      </v-card-text>
      <v-card-actions>
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
export default {
  props: {
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
    };
  },
  computed: {
    chartSettings() {
      return Object.assign({}, this.settings);
    },
    settingJsonContent() {
      return this.settings && JSON.stringify(this.chartSettings, null, 2);
    },
  },
  methods: {
    open() {
      this.dialog = true;
    },
  },
};
</script>
