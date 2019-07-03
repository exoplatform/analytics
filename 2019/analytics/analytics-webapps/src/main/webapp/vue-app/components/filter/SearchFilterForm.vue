<template>
  <v-layout column>
    <v-flex xs12 class="text-xs-center">
      <h3>Search Filter</h3>
    </v-flex>
    <v-flex xs12>
      <v-form
        ref="form"
        class="searchFilterForm"
        @submit="
          $event.preventDefault();
          $event.stopPropagation();
        ">
        <v-layout v-for="(item, index) in searchFiltersContent" :key="index">
          <v-flex xs2>
            <v-text-field
              v-model="item.field"
              label="Field name"
              required />
          </v-flex>
          <v-flex xs2 class="d-flex mx-2">
            <select
              v-model="item.type"
              label="Operator"
              class="operatorInput">
              <option
                v-for="option in searchFilterTypes"
                :key="option.value"
                :value="option.value"
                :placeholder="option.placeholder">
                {{ option.text }}
              </option>
            </select>
          </v-flex>
          <v-flex v-if="item.type === 'EQUAL' || item.type === 'LESS' || item.type === 'GREATER'" xs2>
            <v-text-field
              v-model="item.valueString"
              label="Value"
              required />
          </v-flex>
          <v-flex v-else-if="item.type === 'IN_SET'" xs4>
            <v-text-field
              v-model="item.valuesString"
              label="Values"
              required />
          </v-flex>
          <template v-else-if="item.type === 'RANGE' && (item.range || (item.range = {}))">
            <v-flex xs2>
              <v-text-field
                v-model="item.range.min"
                label="Min"
                required />
            </v-flex>
            <v-flex xs2>
              <v-text-field
                v-model="item.range.max"
                label="Max"
                required />
            </v-flex>
          </template>
          <v-flex :class="((item.type === 'EQUAL' || item.type === 'LESS' || item.type === 'GREATER') && 'offset-xs2') || ((item.type === 'IS_NULL' || item.type === 'NOT_NULL') && 'offset-xs4')" xs1>
            <v-btn icon @click="deleteFilter(index)">
              <v-icon>fa-minus-circle</v-icon>
            </v-btn>
          </v-flex>
          <v-flex v-if="(index +1) === searchFiltersContent.length" xs1>
            <v-btn icon @click="addFilter">
              <v-icon>fa-plus-circle</v-icon>
            </v-btn>
          </v-flex>
        </v-layout>
      </v-form>
    </v-flex>
  </v-layout>
</template>

<script>

export default {
  props: {
    searchFiltersContent: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data: () => ({
    searchFilterTypes: [
      {
        text: 'equals',
        value: 'EQUAL',
        placeholder: 'Exact equals to value',
      },
      {
        text: 'in values',
        value: 'IN_SET',
        placeholder: 'in a set of values (separator \',\')',
      },
      {
        text: 'in range',
        value: 'RANGE',
        placeholder: 'Between \'min\' and \'max\'',
      },
      {
        text: 'less',
        value: 'LESS',
        placeholder: 'less or equals than',
      },
      {
        text: 'greater',
        value: 'GREATER',
        placeholder: 'greater or equals than',
      },
      {
        text: 'is null',
        value: 'IS_NULL',
        placeholder: 'is empty',
      },
      {
        text: 'is not null',
        value: 'NOT_NULL',
        placeholder: 'Is not null',
      },
    ],
  }),
  methods: {
    deleteFilter(searchFilterIndex){
      this.searchFiltersContent.splice(searchFilterIndex, 1);
    },
    addFilter(){
      this.searchFiltersContent.push({});
    },
  },
};
</script>