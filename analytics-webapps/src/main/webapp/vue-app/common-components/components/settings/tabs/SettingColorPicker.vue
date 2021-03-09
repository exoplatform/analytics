<template>
  <v-dialog
    ref="dialog"
    v-model="modal"
    color="white"
    width="290px"
    content-class="overflow-hidden">
    <template v-slot:activator="{ on }">
      <div class="themeColor">
        <div
          :style="{ backgroundColor: value}"
          class="colorSelected"
          v-on="on">
        </div>
        <div class="themeDetails">
          <label
            :title="$t('analytics.label.clickToChange')"
            :style="`background-color: ${value};`"
            class="pa-3 box-border-sizing border-radius white--text"
            v-on="on">{{ value }}</label>
        </div>
      </div>
    </template>
    <v-color-picker
      v-model="value"
      :swatches="swatches"
      show-swatches
      mode="hexa"
      dot-size="25"
      @update:color="$emit('updated', value)" />
    <v-row class="mx-0 white">
      <v-col class="center pt-0">
        <v-btn
          text
          color="primary"
          @click="cancel">
          Cancel
        </v-btn>
      </v-col>
      <v-col class="center pt-0">
        <v-btn
          text
          color="primary"
          @click="save">
          OK
        </v-btn>
      </v-col>
    </v-row>
  </v-dialog>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: function() {
        return null;
      },
    },
    value: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    modal: false,
    originalValue: null,
    swatches: [
      ['#ffaacc', '#f97575', '#98cc81'],
      ['#4273c8', '#cea6ac', '#bc99e7'],
      ['#9ee4f5', '#774ea9', '#ffa500'],
      ['#bed67e', '#0E100F', '#319ab3'],
    ],
  }),
  watch: {
    modal() {
      if (this.modal) {
        this.originalValue = this.value;
      }
    }
  },
  methods: {
    cancel() {
      this.value = this.originalValue;
      this.modal = false;
    },
    save() {
      this.$emit('input', this.value);
      this.modal = false;
    }
  }
};
</script>
