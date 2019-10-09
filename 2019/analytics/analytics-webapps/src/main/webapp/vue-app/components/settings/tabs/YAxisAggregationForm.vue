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
          <v-radio-group v-model="yAxisAggregation.type">
            <v-radio
              v-for="aggregationType in aggregationTypes"
              :key="aggregationType.value"
              :label="aggregationType.text"
              :value="aggregationType.value" />
          </v-radio-group>
        </v-flex>
        <v-flex
          v-if="!yAxisAggregationCount"
          class="my-auto px-2"
          xs6>
          <field-selection
            v-model="yAxisAggregation.field"
            label="Aggragation field"
            aggregation
            number />
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
    aggregationTypes: [
      {
        text: 'Count samples',
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
    yAxisAggregationCount() {
      return this.yAxisAggregation && this.yAxisAggregation.type === 'COUNT';
    }
  },
  watch: {
    yAxisAggregationCount() {
      this.yAxisAggregation.field = null;
    }
  },
  mounted() {
    if (!this.yAxisAggregation.type) {
      this.yAxisAggregation.type = 'COUNT';
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