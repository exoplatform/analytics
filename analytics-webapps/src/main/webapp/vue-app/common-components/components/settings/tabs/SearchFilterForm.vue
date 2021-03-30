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
          <v-layout v-for="(item, index) in filters" :key="item">
            <v-flex xs4 class="my-auto">
              <analytics-field-selection
                v-model="item.field"
                :fields-mappings="fieldsMappings"
                :placeholder="$t('analytics.fieldNamePlaceholder')"
                :attach="attach" />
            </v-flex>
            <v-flex xs3 class="d-flex my-auto px-2 content-box-sizing">
              <select
                v-model="item.type"
                :placeholder="$t('analytics.operatorPlaceholder')"
                class="operatorInput border-color ma-auto width-auto ignore-vuetify-classes"
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
              <input
                v-if="item.type === 'EQUAL' || item.type === 'NOT_EQUAL'"
                v-model="item.valueString"
                type="text"
                :placeholder="$t('analytics.numberValuePlaceholder')"
                class="ignore-vuetify-classes width-auto pa-0 my-auto"
                required>
              <input
                v-else-if="item.type === 'LESS' || item.type === 'GREATER'"
                v-model.number="item.valueString"
                type="number"
                :placeholder="$t('analytics.valuePlaceholder')"
                class="ignore-vuetify-classes width-auto pa-0 my-auto"
                required>
              <input
                v-else-if="item.type === 'IN_SET' || item.type === 'NOT_IN_SET'"
                v-model="item.valueString"
                type="text"
                class="ignore-vuetify-classes width-auto pa-0 my-auto"
                :placeholder="$t('analytics.setPlaceholder')"
                required>
              <template v-else-if="item.type === 'RANGE' && (item.range || (item.range = {}))">
                <input
                  v-model.number="item.range.min"
                  type="number"
                  :placeholder="$t('analytics.minPlaceholder')"
                  class="ignore-vuetify-classes width-auto pa-0 my-auto"
                  required>
                <input
                  v-model.number="item.range.max"
                  type="number"
                  :placeholder="$t('analytics.maxPlaceholder')"
                  class="ignore-vuetify-classes pa-0 width-auto my-auto"
                  required>
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
export default {
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
    attach: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    searchFilterTypes() {
      return [
        {
          text: this.$t('analytics.equals'),
          value: 'EQUAL',
          placeholder: this.$t('analytics.fieldValuePlaceholder'),
        },
        {
          text: this.$t('analytics.notEquals'),
          value: 'NOT_EQUAL',
          placeholder: this.$t('analytics.fieldValuePlaceholder'),
        },
        {
          text: this.$t('analytics.inValues'),
          value: 'IN_SET',
          placeholder: this.$t('analytics.setPlaceholder'),
        },
        {
          text: this.$t('analytics.notInValues'),
          value: 'NOT_IN_SET',
          placeholder: this.$t('analytics.setPlaceholder'),
        },
        {
          text: this.$t('analytics.inRange'),
          value: 'RANGE',
          placeholder: this.$t('analytics.rangePlaceholder'),
        },
        {
          text: this.$t('analytics.less'),
          value: 'LESS',
          placeholder: this.$t('analytics.lessPlaceholder'),
        },
        {
          text: this.$t('analytics.greater'),
          value: 'GREATER',
          placeholder: this.$t('analytics.greaterPlaceholder'),
        },
        {
          text: this.$t('analytics.isNull'),
          value: 'IS_NULL',
          placeholder: this.$t('analytics.isEmptyPlaceholder'),
        },
        {
          text: this.$t('analytics.isNotNull'),
          value: 'NOT_NULL',
          placeholder: this.$t('analytics.isNotNullPlaceholder'),
        },
      ];
    }
  },
  methods: {
    deleteFilter(searchFilterIndex){
      this.filters.splice(searchFilterIndex, 1);
      this.$forceUpdate();
    },
    addFilter(){
      this.filters.push({type: 'EQUAL'});
      this.$forceUpdate();
    },
  },
};
</script>