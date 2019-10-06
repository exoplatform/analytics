<template>
  <v-layout column>
    <v-flex xs12>
      <v-form
        ref="form"
        class="searchFilterForm"
        @submit="
          $event.preventDefault();
          $event.stopPropagation();
        ">
        <template v-if="filters && filters.length">
          <v-layout v-for="(item, index) in filters" :key="index">
            <v-flex xs2>
              <v-text-field
                v-model="item.field"
                label="Field name"
                required />
            </v-flex>
            <v-flex xs4 class="my-auto px-2">
              <v-select
                v-model="item.type"
                :items="searchFilterTypes"
                :item-text="text"
                :item-value="value"
                :value-comparator="selectedValueComparator"
                class="operatorInput"
                hide-selected
                persistent-hint
                chips
                label="Operator" />
            </v-flex>
            <v-flex
              v-if="item.type === 'EQUAL' || item.type === 'LESS' || item.type === 'GREATER'"
              xs2
              class="my-auto">
              <v-text-field
                v-model="item.valueString"
                label="Value"
                required />
            </v-flex>
            <v-flex
              v-else-if="item.type === 'IN_SET'"
              xs4
              class="my-auto">
              <v-text-field
                v-model="item.valuesString"
                label="Values"
                required />
            </v-flex>
            <template v-else-if="item.type === 'RANGE' && (item.range || (item.range = {}))" class="my-auto">
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
            <v-flex
              :class="((item.type === 'EQUAL' || item.type === 'LESS' || item.type === 'GREATER') && 'offset-xs2') || ((item.type === 'IS_NULL' || item.type === 'NOT_NULL') && 'offset-xs4')"
              xs1
              class="my-auto">
              <v-btn icon @click="deleteFilter(index)">
                <v-icon>fa-minus-circle</v-icon>
              </v-btn>
            </v-flex>
            <v-flex
              v-if="(index +1) === filters.length"
              class="my-auto"
              xs1>
              <v-btn icon @click="addFilter">
                <v-icon>fa-plus-circle</v-icon>
              </v-btn>
            </v-flex>
          </v-layout>
        </template>
        <v-layout v-else>
          <v-flex class="my-auto" xs1>
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
    filters: {
      type: Array,
      default: function() {
        return null;
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
      this.filters.splice(searchFilterIndex, 1);
    },
    addFilter(){
      this.filters.push({type: 'equals'});
    },
  },
};
</script>