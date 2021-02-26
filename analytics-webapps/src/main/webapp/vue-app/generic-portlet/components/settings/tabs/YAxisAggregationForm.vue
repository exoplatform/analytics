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
          <v-radio-group v-model="aggregationType">
            <v-radio
              v-for="aggregationType in aggregationTypes"
              :key="aggregationType.value"
              :label="aggregationType.text"
              :value="aggregationType.value" />
          </v-radio-group>
        </v-flex>
        <v-flex
          v-show="!yAxisAggregationCount"
          class="my-auto px-2"
          xs6>
          <analytics-field-selection
            v-model="yAxisAggregation.field"
            :fields-mappings="fieldsMappings"
            :placeholder="yAxisAggregationCardinality ? $t('analytics.distinctAggregationField') : $t('analytics.numericAggregationField')"
            :numeric="!yAxisAggregationCardinality"
            aggregation />
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
    fieldsMappings: {
      type: Array,
      default: function() {
        return [];
      },
    },
    type:{
      type: String,
      default: function (){
        return null;
      }
    }
  },
  data: () => ({
    aggregationType: 'MAX',
  }),
  computed: {
    aggregationTypes() {
      return [
        {
          text: this.$t('analytics.count'),
          value: 'COUNT',
        },
        {
          text: this.$t('analytics.cardinality'),
          value: 'CARDINALITY',
        },
        {
          text: this.$t('analytics.sum'),
          value: 'SUM',
        },
        {
          text: this.$t('analytics.avg'),
          value: 'AVG',
        },
        {
          text: this.$t('analytics.max'),
          value: 'MAX',
        },
        {
          text: this.$t('analytics.min'),
          value: 'MIN',
        },
      ];
    },
    yAxisAggregation() {
      if (this.type === 'value'){
        return this.settings && this.settings.value.yAxisAggregation;
      }else if (this.type === 'threshold'){
        return this.settings && this.settings.threshold.yAxisAggregation;
      }else {
        return this.settings && this.settings.yAxisAggregation;
      }
    },
    yAxisAggregationCount() {
      return this.aggregationType === 'COUNT';
    },
    yAxisAggregationCardinality() {
      return this.aggregationType === 'CARDINALITY';
    },
  },
  watch: {
    yAxisAggregationCount() {
      this.yAxisAggregation.field = null;
    },
    aggregationType() {
      this.yAxisAggregation.type = this.aggregationType;
    },
  },
  created() {
    if (this.yAxisAggregation.type) {
      this.aggregationType = this.yAxisAggregation.type;
    } else {
      this.aggregationType = 'COUNT';
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