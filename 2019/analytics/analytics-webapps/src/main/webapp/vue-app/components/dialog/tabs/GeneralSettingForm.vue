<template>
  <v-form
    ref="form"
    class="generalSettingForm"
    @submit="
      $event.preventDefault();
      $event.stopPropagation();
    ">
    <v-layout wrap xs12>
      <v-flex
        class="my-auto px-2"
        md6
        xs12>
        <v-text-field
          v-model="settings.title"
          label="Chart title"
          required />
      </v-flex>
      <v-flex
        class="my-auto px-2"
        md6
        xs12>
        <v-select
          v-model="settings.chartType"
          :items="chartTypes"
          :item-text="text"
          :item-value="value"
          :value-comparator="selectedValueComparator"
          label="Chart type"
          class="operatorInput"
          hide-selected
          persistent-hint
          chips />
      </v-flex>
      <v-flex
        class="my-auto px-2"
        md6
        xs12>
        <v-switch
          v-model="settings.displayComputingTime"
          label="Display computing time"
          required />
      </v-flex>
      <v-flex
        class="my-auto px-2"
        md6
        xs12>
        <v-switch
          v-model="settings.displaySamplesCount"
          label="Display samples count"
          required />
      </v-flex>
    </v-layout>
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
    chartTypes: [
      {
        text: 'Bars',
        value: 'bar',
      },
      {
        text: 'Lines',
        value: 'line',
      },
      {
        text: 'Pie',
        value: 'pie',
      },
    ],
  }),
};
</script>