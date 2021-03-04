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
          :label="$t('analytics.chartTitle')"
          required />
      </v-flex>
      <v-flex
        class="my-auto px-2"
        md6
        xs12>
        <v-select
          v-model="settings.chartType"
          :items="chartTypes"
          :value-comparator="selectedValueComparator"
          item-text="text"
          item-value="value"
          :label="$t('analytics.chartType')"
          class="operatorInput"
          persistent-hint
          chips
          @change="$emit('type-changed')" />
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
  computed: {
    isPercentageBar() {
      return this.settings.chartType === 'percentageBar' || this.settings.chartType === 'percentage';
    },
    chartTypes(){
      if (this.isPercentageBar){
        return [
          {
            text: 'Percentage Bar',
            value: 'percentageBar',
          },
          {
            text: 'Percentage',
            value: 'percentage',
          }
        ];
      }else {
        return [
          {
            text: this.$t('analytics.bar'),
            value: 'bar',
          },
          {
            text: this.$t('analytics.line'),
            value: 'line',
          },
          {
            text: this.$t('analytics.pie'),
            value: 'pie',
          },
        ];
      }

    }
  },
  methods: {
    selectedValueComparator(item1, item2){
      const item1Value = (item1 && item1.value) || item1;
      const item2Value = (item2 && item2.value) || item2;
      return item1Value === item2Value;
    },
  },
};
</script>