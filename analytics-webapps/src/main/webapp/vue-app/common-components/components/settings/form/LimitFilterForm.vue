<template>
  <div>
    <v-divider class="my-4" />
    <h3>{{ $t('analytics.limitFilters') }}</h3>
    <v-switch
      v-model="limitResults"
      :label="$t('analytics.limitResults')"
      class="my-auto text-no-wrap" />
    <div class="d-flex flex-row">
      <template v-if="limitResults && percentageLimit">
        <input
          v-model="percentage"
          type="number"
          min="0"
          max="100"
          step="0.1"
          class="ignore-vuetify-classes inputSmall my-auto">
        <span class="my-auto ml-1">%</span>
        <span class="my-auto ml-1">{{ $t('analytics.ofField') }}</span>
        <analytics-field-selection
          v-model="field"
          :fields-mappings="fieldsMappings"
          aggregation
          class="ml-1" />
      </template>
    </div>
    <v-divider class="my-4" />
    <template v-if="limitResults && percentageLimit">
      <h3>{{ $t('analytics.limitComputingRule') }}</h3>
      <analytics-y-axis-form
        :fields-mappings="fieldsMappings"
        :y-axis-aggregation="yAxisAggregation" />
      <v-divider class="my-4" />
      <h3>{{ $t('analytics.limitComputingFilters') }}</h3>
      <analytics-search-filter-form
        :fields-mappings="fieldsMappings"
        :filters="filters" />
    </template>
  </div>
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
  },
  data: () => ({
    limitResults: false,
    percentage: 0,
    field: null,
    yAxisAggregation: null,
    filters: null,
  }),
  computed: {
    percentageLimit() {
      return this.settings && this.settings.percentageLimit;
    },
  },
  watch: {
    field() {
      if (this.percentageLimit) {
        this.percentageLimit.field = this.field;
      }
    },
    percentage() {
      if (this.percentageLimit) {
        this.percentageLimit.percentage = this.percentage;
      }
    },
    limitResults() {
      if (this.limitResults) {
        if (this.percentageLimit) {
          return;
        }
        this.filters = [];
        this.yAxisAggregation = {
          type: 'COUNT',
        };
        this.field = null;
        this.percentage = 0;
        this.settings.percentageLimit = {
          aggregation: {
            yAxisAggregation: this.yAxisAggregation,
            filters: this.filters,
          },
          percentage: this.percentage,
          field: this.field,
        };
      } else {
        this.settings.percentageLimit = null;
      }
    },
  },
  created() {
    if (this.percentageLimit && this.percentageLimit.aggregation) {
      this.percentage = this.percentageLimit.percentage || 0;
      this.field = this.percentageLimit.field || null;
      this.filters = this.percentageLimit.aggregation.filters || [];
      this.yAxisAggregation = this.percentageLimit.aggregation.yAxisAggregation || {
        type: 'COUNT',
      };
      this.limitResults = true;
    }
  },
};
</script>