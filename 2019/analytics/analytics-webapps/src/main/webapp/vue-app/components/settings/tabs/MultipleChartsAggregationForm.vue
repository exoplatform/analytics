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
            :disabled="disabled"
            label="Multiple charts"
            required />
        </v-flex>
        <v-flex
          v-if="multipleCharts && !disabled"
          class="my-auto px-2"
          xs6>
          <field-selection
            v-model="settings.multipleChartsField"
            label="Field comparator"
            aggregation />
        </v-flex>
      </v-layout>
    </v-card-text>
  </v-form>
</template>

<script>
import FieldSelection from '../form/FieldSelection.vue';

export default {
  components: {
    FieldSelection,
  },
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
    disabled: false,
  }),
  computed: {
    yAxisAggregation() {
      return this.settings && this.settings.yAxisAggregation;
    },
  },
  watch: {
    disabled() {
      if (this.disabled) {
        this.settings.multipleChartsField = null;
      }
    },
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
    this.update();
  },
  methods: {
    update() {
      // To disable multiple chart for 'pie' type
      // this.disabled = this.settings && this.settings.chartType !== 'line' && this.settings.chartType !== 'bar';
      this.multipleCharts = !this.disabled && this.settings && this.settings.multipleChartsField;
    }
  },
};
</script>