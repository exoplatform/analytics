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
            <v-flex xs4 class="my-auto">
              <field-selection
                v-model="item.field"
                :fields-mappings="fieldsMappings"
                label="Field name" />
            </v-flex>
            <v-flex xs3 class="my-auto px-2">
              <v-select
                v-model="item.type"
                :items="searchFilterTypes"
                :value-comparator="selectedValueComparator"
                :return-object="false"
                item-text="text"
                item-value="value"
                class="operatorInput"
                hide-selected
                persistent-hint
                chips
                label="Operator" />
            </v-flex>
            <v-flex xs3 class="my-auto">
              <v-text-field
                v-if="item.type === 'EQUAL' || item.type === 'LESS' || item.type === 'GREATER'"
                v-model="item.valueString"
                label="Value"
                required />
              <v-text-field
                v-else-if="item.type === 'IN_SET'"
                v-model="item.valuesString"
                label="Values"
                placeholder="Values (Separator ',')"
                required />
              <template v-else-if="item.type === 'RANGE' && (item.range || (item.range = {}))">
                <v-text-field
                  v-model="item.range.min"
                  label="Min"
                  required />
                <v-text-field
                  v-model="item.range.max"
                  label="Max"
                  required />
              </template>
            </v-flex>
            <v-flex xs1 class="my-auto">
              <v-btn icon @click="deleteFilter(index)">
                <v-icon>fa-minus-circle</v-icon>
              </v-btn>
            </v-flex>
            <v-flex class="my-auto" xs1>
              <v-btn
                v-if="(index +1) === filters.length"
                icon
                @click="addFilter">
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
import FieldSelection from '../form/FieldSelection.vue';

export default {
  components: {
    FieldSelection,
  },
  props: {
    filters: {
      type: Array,
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
    selectedValueComparator(item1, item2){
      const item1Value = item1 && item1.value || item1;
      const item2Value = item2 && item2.value || item2;
      return item1Value === item2Value;
    },
    deleteFilter(searchFilterIndex){
      this.filters.splice(searchFilterIndex, 1);
    },
    addFilter(){
      this.filters.push({type: 'equals'});
    },
  },
};
</script>