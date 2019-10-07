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
          <v-select
            v-model="yAxisAggregation.type"
            :items="aggregationTypes"
            :item-text="text"
            :item-value="value"
            :value-comparator="selectedValueComparator"
            label="Aggregation type"
            class="operatorInput"
            hide-selected
            persistent-hint
            chips
            @change="$forceUpdate()" />
        </v-flex>
        <v-flex class="my-auto px-2" xs6>
          <v-text-field
            v-if="yAxisAggregation.type !== 'COUNT'"
            v-model="yAxisAggregation.field"
            label="Aggragation field"
            required />
        </v-flex>
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
    aggregationTypes: [
      {
        text: 'Count results',
        value: 'COUNT',
      },
      {
        text: 'Sum of',
        value: 'SUM',
      },
      {
        text: 'Average of',
        value: 'AVG',
      },
    ],
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
    if (!this.yAxisAggregation.type) {
      this.yAxisAggregation.type = 'COUNT';
    }
  },
};
</script>