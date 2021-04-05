<template>
  <v-layout column>
    <v-flex xs12 class="d-flex flex-nowrap">
      <span v-if="!noTitle" class="me-2">
        <template v-if="$slots.title">
          <slot name="title"></slot>
        </template>
        <h4 v-else>{{ $t('analytics.dataFilters') }}</h4>
      </span>
      <v-btn
        icon
        class="my-auto"
        @click="addFilter">
        <v-icon>fa-plus-circle</v-icon>
      </v-btn>
    </v-flex>
    <v-flex v-if="filters && filters.length" xs12>
      <v-form
        ref="form"
        class="searchFilterForm"
        @submit="
          $event.preventDefault();
          $event.stopPropagation();
        ">
        <analytics-field-filter
          v-for="(filter, index) in filters"
          :key="filter"
          :filter="filter"
          :fields-mappings="fieldsMappings"
          :attach="attach"
          class="my-2"
          @delete="deleteFilter(index)" />
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
    noTitle: {
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