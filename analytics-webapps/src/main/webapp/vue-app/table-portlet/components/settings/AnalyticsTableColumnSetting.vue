<template>
  <v-list-group>
    <template v-slot:activator>
      <v-list-item-title v-if="columnTitle">{{ columnTitle }}</v-list-item-title>
      <v-list-item-title v-else class="font-italic subtitle-1 text-sub-title">{{ $t('analytics.columnTitlePlaceholder') }}</v-list-item-title>
    </template>
    <div class="border-radius border-color">
      <v-card-text>
        <template v-if="canEnableIdentityFields">
          <v-switch
            v-model="useIdentityField"
            :label="useIdentityFieldLabel"
            class="my-auto text-no-wrap" />
          <template v-if="useIdentityField">
            <v-autocomplete
              v-if="useUserField"
              v-model="column.userField"
              :return-object="false"
              :items="userFields"
              :label="$t('analytics.userField')"
              :value-comparator="selectedValueComparator"
              item-text="label"
              item-value="name"
              return-value
              class="pt-0 pb-3"
              filled
              persistent-hint
              dense
              chips
              @change="updateColumnDataType('user')" />
            <v-autocomplete
              v-else-if="useSpaceField"
              v-model="column.spaceField"
              :return-object="false"
              :items="spaceFields"
              :label="$t('analytics.userField')"
              :value-comparator="selectedValueComparator"
              class="pt-0 pb-3"
              item-text="label"
              item-value="name"
              return-value
              filled
              persistent-hint
              outlined
              dense
              chips
              @change="updateColumnDataType('space')" />
          </template>
        </template>
        <template v-if="!canEnableIdentityFields || !useIdentityField">
          <v-text-field
            v-model="column.title"
            :label="$t('analytics.columnTitle')"
            outlined
            required />
          <v-switch
            v-show="!isTermsAggregation"
            v-model="column.previousPeriod"
            :label="$t('analytics.compareWithPreviousPeriod')"
            class="my-2 text-no-wrap" />
          <h4>{{ $t('analytics.computingRule') }}</h4>
          <v-layout no-wrap>
            <v-flex class="my-auto px-2">
              <v-radio-group v-model="aggregationType">
                <v-radio
                  v-for="aggregationType in aggregationTypes"
                  :key="aggregationType.value"
                  :label="aggregationType.text"
                  :value="aggregationType.value" />
              </v-radio-group>
            </v-flex>
            <v-flex
              v-show="!isAggregationCount"
              class="my-auto px-2">
              <analytics-field-selection
                v-model="column.aggregation.field"
                :fields-mappings="fieldsMappings"
                :placeholder="numericAggregationField ? $t('analytics.numericAggregationField') : $t('analytics.distinctAggregationField')"
                :numeric="numericAggregationField"
                aggregation
                @change="updateColumnDataType('aggregation')" />
              <v-flex v-if="isTermsAggregation" class="px-2 mt-2 d-flex flex-row">
                <v-switch
                  v-model="limitResults"
                  :label="$t('analytics.limitResults')"
                  class="my-auto text-no-wrap" />
                <input
                  v-if="limitResults"
                  v-model="column.aggregation.limit"
                  type="number"
                  class="ignore-vuetify-classes inputSmall mt-1 ml-3">
                <select
                  v-model="column.aggregation.sortDirection"
                  class="ignore-vuetify-classes ml-2 mt-1 width-auto">
                  <option value="desc">{{ $t('analytics.descending') }}</option>
                  <option value="asc">{{ $t('analytics.ascending') }}</option>
                </select>
              </v-flex>
            </v-flex>
          </v-layout>
          <h4>{{ $t('analytics.dataFilters') }}</h4>
          <analytics-search-filter-form
            :fields-mappings="fieldsMappings"
            :filters="column.filters" />
        </template>
      </v-card-text>
    </div>
  </v-list-group>
</template>
<script>
export default {
  props: {
    mainColumn: {
      type: Object,
      default: function() {
        return null;
      },
    },
    column: {
      type: Object,
      default: function() {
        return null;
      },
    },
    userFields: {
      type: Array,
      default: function() {
        return null;
      },
    },
    spaceFields: {
      type: Array,
      default: function() {
        return null;
      },
    },
    fieldsMappings: {
      type: Array,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    useIdentityField: false,
    aggregationType: 'MAX',
    limitResults: false,
  }),
  computed: {
    columnTitle() {
      return this.column && this.column.title;
    },
    canEnableIdentityFields() {
      return this.mainColumn && this.mainColumn.aggregation
        && (this.mainColumn.aggregation.field === 'userId'
            || this.mainColumn.aggregation.field === 'spaceId');
    },
    useUserField() {
      return this.canEnableIdentityFields && this.mainColumn.aggregation.field === 'userId';
    },
    useSpaceField() {
      return this.canEnableIdentityFields && this.mainColumn.aggregation.field === 'spaceId';
    },
    useIdentityFieldLabel() {
      if (this.useUserField) {
        return this.$t('analytics.displayUserField');
      } else if (this.useSpaceField) {
        return this.$t('analytics.displaySpaceField');
      }
      return null;
    },
    aggregationTypes() {
      return [
        {
          text: this.$t('analytics.perCustomField'),
          value: 'TERMS',
        },
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
    isAggregationCount() {
      return this.aggregationType === 'COUNT';
    },
    isTermsAggregation() {
      return this.aggregationType === 'TERMS';
    },
    numericAggregationField() {
      return this.aggregationType !== 'CARDINALITY' && this.aggregationType !== 'TERMS';
    },
  },
  watch: {
    limitResults() {
      if (this.limitResults) {
        this.column.aggregation.limit = 1;
      } else {
        this.column.aggregation.limit = 0;
      }
    },
    isAggregationCount() {
      if (this.column.aggregation) {
        this.column.aggregation.field = null;
      }
    },
    aggregationType() {
      if (this.column.aggregation) {
        this.column.aggregation.type = this.aggregationType;
      }
    },
    canEnableIdentityFields() {
      if(!this.canEnableIdentityFields) {
        this.column.userField = null;
        this.column.spaceField = null;
      }
    },
  },
  created() {
    if (this.column && this.column.aggregation && this.column.aggregation.type) {
      this.aggregationType = this.column.aggregation.type;
    } else {
      this.aggregationType = 'COUNT';
    }
    this.useIdentityField = this.canEnableIdentityFields && this.column && (this.column.userField || this.column.spaceField);
    this.limitResults = this.column.aggregation && this.column.aggregation.limit;
    if(!this.canEnableIdentityFields) {
      this.column.userField = null;
      this.column.spaceField = null;
    }
  },
  methods: {
    updateColumnDataType(field) {
      if (field === 'user' && this.column.userField) {
        this.column.spaceField = null;
        this.column.aggregation.field = null;
        const columnMaping = this.userFields.find(mapping => mapping && mapping.name === this.column.userField);
        this.column.dataType = columnMaping && columnMaping.type;
        this.column.sortable = false;
      } if (field === 'space' && this.column.spaceField) {
        this.column.userField = null;
        this.column.aggregation.field = null;
        const columnMaping = this.spaceFields.find(mapping => mapping && mapping.name === this.column.spaceField);
        this.column.dataType = columnMaping && columnMaping.type;
        this.column.sortable = false;
      } else if (this.column.aggregation.field) {
        this.column.userField = null;
        this.column.spaceField = null;
        const columnMaping = this.fieldsMappings.find(mapping => mapping && mapping.name === this.column.aggregation.field);
        this.column.dataType = columnMaping && columnMaping.type;
        this.column.sortable = this.column.aggregation.type !== 'TERMS';
      }
    },
    selectedValueComparator(item1, item2){
      const item1Value = (item1 && item1.field) || item1;
      const item2Value = (item2 && item2.field) || item2;
      return item1Value === item2Value;
    },
  },
};
</script>