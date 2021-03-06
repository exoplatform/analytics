<template>
  <v-form
    ref="form"
    class="aggregationForm"
    @submit="
      $event.preventDefault();
      $event.stopPropagation();
    ">
    <v-card-text>
      <v-layout wrap>
        <template>
          <v-flex
            class="my-auto px-2"
            xs4>
            <v-radio-group v-model="dateInterval">
              <v-radio
                v-for="dateField in dateFields"
                :key="dateField.value"
                :label="dateField.text"
                :value="dateField.value" />
              <v-radio :label="$t('analytics.perCustomField')" value="custom" />
            </v-radio-group>
          </v-flex>
          <v-flex
            v-if="!dateAggregationType"
            class="my-auto px-2"
            xs8>
            <analytics-x-axis-aggregation-field
              v-for="(aggregation, index) in xAxisAggregations"
              :key="index"
              :aggregation="aggregation"
              :fields-mappings="fieldsMappings"
              :display-add="(index +1) === xAxisAggregations.length"
              :display-delete="xAxisAggregations.length > 1"
              @delete="deleteAggregation(index)"
              @add="addAggregation" />
          </v-flex>
        </template>
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
    fieldsMappings: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data: () => ({
    dateAggregationType: true,
    dateInterval: 'custom',
    dateAggregation: {
      type: 'DATE',
      field: 'timestamp',
      sortDirection: 'asc',
      interval: 'day',
    },
    fieldAggregation: {
      type: 'TERMS',
      field: 'module',
      sortDirection: 'desc',
      limit: 0,
    },
  }),
  computed: {
    dateFields() {
      return [
        {
          text: this.$t('analytics.perDay'),
          value: 'day',
        },
        {
          text: this.$t('analytics.perWeek'),
          value: 'week',
        },
        {
          text: this.$t('analytics.perMonth'),
          value: 'month',
        },
        {
          text: this.$t('analytics.perQuarter'),
          value: 'quarter',
        },
        {
          text: this.$t('analytics.perYear'),
          value: 'year',
        },
      ];
    } ,
    xAxisAggregations() {
      return this.settings && this.settings.xAxisAggregations;
    },
    xAxisAggregation() {
      return this.hasAggregations && this.settings.xAxisAggregations[0] || null;
    },
    hasAggregations() {
      return this.xAxisAggregations && this.xAxisAggregations.length;
    }
  },
  watch: {
    dateInterval(newVal, oldVal) {
      if (oldVal !== newVal) {
        this.dateAggregationType = this.dateInterval !== 'custom';
        this.changeAggregationType();
      }
    }
  },
  mounted() {
    if (!this.xAxisAggregations || !this.xAxisAggregations.length) {
      this.dateInterval = 'day';
    } else {
      this.dateAggregationType = this.xAxisAggregations && this.xAxisAggregations.length === 1 && this.xAxisAggregations[0].type === 'DATE';
      if (this.dateAggregationType) {
        this.dateInterval = this.xAxisAggregations[0].interval;
      }
    }
  },
  methods: {
    changeAggregationType() {
      if (this.dateAggregationType) {
        const dateAggregation = Object.assign({}, this.dateAggregation);
        dateAggregation.interval = this.dateInterval;
        this.settings.xAxisAggregations.splice(0, this.settings.xAxisAggregations.length, dateAggregation);
      } else {
        this.settings.xAxisAggregations.splice(0, this.settings.xAxisAggregations.length, Object.assign({}, this.fieldAggregation));
      }
      this.$forceUpdate();
    },
    deleteAggregation(aggregationIndex) {
      this.xAxisAggregations.splice(aggregationIndex, 1);
    },
    addAggregation() {
      this.xAxisAggregations.push({
        type: 'TERMS',
        sortDirection: 'desc',
        limit: 0,
      });
      this.$forceUpdate();
    },
  },
};
</script>