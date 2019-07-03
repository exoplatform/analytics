<template>
  <v-layout column>
    <v-flex xs12 class="text-xs-center">
      <h3>Search Aggregation</h3>
    </v-flex>
    <v-flex xs12>
      <v-form
        ref="form"
        class="aggregationForm"
        @submit="
          $event.preventDefault();
          $event.stopPropagation();
        ">
        <v-layout v-for="(item, index) in aggregations" :key="index">
          <v-flex md2>
            <v-text-field
              v-model="item.field"
              label="Field name"
              required />
          </v-flex>
          <v-flex
            v-if="(index +1) === aggregations.length"
            class="d-flex mx-2"
            md2>
            <select
              v-model="item.type"
              label="Operator"
              class="operatorInput">
              <option
                v-for="option in aggregationTypes"
                :key="option.value"
                :value="option.value"
                :placeholder="option.placeholder">
                {{ option.text }}
              </option>
            </select>
          </v-flex>
          <v-flex v-if="item.type === 'EQUAL' || item.type === 'LESS' || item.type === 'GREATER'" md2>
            <v-text-field
              v-model="item.valueString"
              label="Value"
              required />
          </v-flex>
          <v-flex v-else-if="item.type === 'IN_SET'" md4>
            <v-text-field
              v-model="item.valuesString"
              label="Values"
              required />
          </v-flex>
          <template v-if="item.type === 'RANGE' && (item.range || (item.range = {}))">
            <v-flex md2>
              <v-text-field
                v-model="item.range.min"
                label="Min"
                required />
            </v-flex>
            <v-flex md2>
              <v-text-field
                v-model="item.range.max"
                label="Max"
                required />
            </v-flex>
          </template>
        </v-layout>
      </v-form>
    </v-flex>
  </v-layout>
</template>

<script>
export default {
  props: {
    aggregations: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data: () => ({
    aggregationTypes: [
      {
        text: 'count elements',
        value: 'COUNT',
        placeholder: 'count samples',
      },
      {
        text: 'sum',
        value: 'SUM',
        placeholder: 'Sum of a field',
      },
      {
        text: 'average',
        value: 'AVG',
        placeholder: 'Average of a numeric field',
      },
    ],
  }),
  methods: {
    addAggregation(){
      this.aggregations.push({});
    }
  }
};
</script>