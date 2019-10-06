<template>
  <v-form
    ref="form"
    class="aggregationForm"
    @submit="
      $event.preventDefault();
      $event.stopPropagation();
    ">
    <v-card-text>
      <h2 class="title mb-2">X axis</h2>
      <template v-if="xAxisAggregations && xAxisAggregations.length">
        <v-layout v-for="(item, index) in xAxisAggregations" :key="index">
          <v-flex xs10>
            <v-text-field
              v-model="item.field"
              label="Field name"
              required />
          </v-flex>
          <v-flex class="my-auto" xs1>
            <v-btn icon @click="deleteAggregation(index)">
              <v-icon>fa-minus-circle</v-icon>
            </v-btn>
          </v-flex>
          <v-flex
            v-if="(index +1) === xAxisAggregations.length"
            class="my-auto"
            xs1>
            <v-btn icon @click="addAggregation">
              <v-icon>fa-plus-circle</v-icon>
            </v-btn>
          </v-flex>
        </v-layout>
      </template>
      <v-layout v-else>
        <v-flex class="my-auto" xs1>
          <v-btn icon @click="addAggregation">
            <v-icon>fa-plus-circle</v-icon>
          </v-btn>
        </v-flex>
      </v-layout>
    </v-card-text>
    <v-card-text>
      <h2 class="title mb-2">Y axis</h2>
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
    xAxisAggregations() {
      return this.settings && this.settings.xAxisAggregations;
    },
    yAxisAggregation() {
      return this.settings && this.settings.yAxisAggregation;
    },
  },
  watch: {
    settings() {
      this.multipleCharts = this.settings && this.settings.multipleChartsField;
    },
  },
  mounted() {
    this.multipleCharts = this.settings && this.settings.multipleChartsField;
    if (!this.yAxisAggregation.type) {
      this.yAxisAggregation.type = 'COUNT';
    }
  },
  methods: {
    deleteAggregation(aggregationIndex) {
      this.xAxisAggregations.splice(aggregationIndex, 1);
    },
    addAggregation() {
      this.xAxisAggregations.push({type: 'COUNT'});
      this.$forceUpdate();
    },
  },
};
</script>