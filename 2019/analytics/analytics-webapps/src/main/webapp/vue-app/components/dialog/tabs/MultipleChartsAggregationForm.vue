<template>
  <v-form
    ref="form"
    class="aggregationForm"
    @submit="
      $event.preventDefault();
      $event.stopPropagation();
    ">
    <v-card-text>
      <v-layout wrap>
        <v-flex class="my-auto px-2" xs6>
          <v-switch
            v-model="multipleCharts"
            label="Multiple charts"
            required />
        </v-flex>
        <v-flex class="my-auto px-2" xs6>
          <v-text-field
            v-if="multipleCharts"
            v-model="settings.multipleChartsField"
            label="Field comparator"
            required />
        </v-flex>
      </v-layout>
    </v-card-text>
  </v-form>
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
  data: () => ({
    multipleCharts: false,
  }),
  computed: {
    yAxisAggregation() {
      return this.settings && this.settings.yAxisAggregation;
    },
  },
  watch: {
    settings() {
      this.multipleCharts = this.settings && this.settings.multipleChartsField;
    },
    multipleCharts() {
      if (!this.multipleCharts) {
        this.settings.multipleChartsField = null;
      }
    },
  },
  mounted() {
    this.multipleCharts = this.settings && this.settings.multipleChartsField;
  },
};
</script>