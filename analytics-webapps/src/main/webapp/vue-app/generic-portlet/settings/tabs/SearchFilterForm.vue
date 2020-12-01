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
            <v-flex xs3 class="mt-auto mb-2 px-2">
              <select
                v-model="item.type"
                label="Operator"
                class="operatorInput border-color width-auto ignore-vuetify-classes"
                @change="$forceUpdate()">
                <option
                  v-for="searchFilterType in searchFilterTypes"
                  :key="searchFilterType.value"
                  :value="searchFilterType.value">
                  {{ searchFilterType.text }}
                </option>
              </select>
            </v-flex>
            <v-flex xs3 class="my-auto">
              <v-text-field
                v-if="item.type === 'EQUAL' || item.type === 'NOT_EQUAL' || item.type === 'LESS' || item.type === 'GREATER'"
                v-model="item.valueString"
                label="Value"
                class="pa-0 my-auto"
                required />
              <v-text-field
                v-else-if="item.type === 'IN_SET' || item.type === 'NOT_IN_SET'"
                v-model="item.valueString"
                label="Values"
                class="pa-0 my-auto"
                placeholder="Values (Separator ',')"
                required />
              <template v-else-if="item.type === 'RANGE' && (item.range || (item.range = {}))">
                <v-text-field
                  v-model="item.range.min"
                  label="Min"
                  class="pa-0 my-auto"
                  required />
                <v-text-field
                  v-model="item.range.max"
                  label="Max"
                  class="pa-0 my-auto"
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
        placeholder: 'field value',
      },
      {
        text: 'not equals',
        value: 'NOT_EQUAL',
        placeholder: 'field value',
      },
      {
        text: 'in values',
        value: 'IN_SET',
        placeholder: 'values (separator \',\')',
      },
      {
        text: 'not in values',
        value: 'NOT_IN_SET',
        placeholder: 'values (separator \',\')',
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
      this.filters.push({type: 'EQUAL'});
      this.$forceUpdate();
    },
  },
};
</script>