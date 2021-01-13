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
        <v-flex class="my-auto px-2" xs6>
          <v-switch
            v-model="multipleCharts"
            :label="$t('analytics.multiple.charts')"
            required />
        </v-flex>
        <v-flex
          v-if="multipleCharts"
          class="my-auto px-2"
          xs6>
          <field-selection
            v-model="settings.multipleChartsField"
            :fields-mappings="fieldsMappings"
            :label="$t('analytics.fieldComparator')"
            aggregation />
        </v-flex>
      </v-layout>
    </v-card-text>
  </v-form>
</template>

<script>
import FieldSelection from '../form/FieldSelection.vue';

export default {
  components: {
    FieldSelection,
  },
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
    multipleCharts: false,
  }),
  computed: {
    yAxisAggregation() {
      return this.settings && this.settings.yAxisAggregation;
    },
  },
  watch: {
    settings() {
      this.multipleCharts = this.settings && this.settings.multipleChartsField;
    },
    multipleCharts() {
      if (!this.multipleCharts) {
        this.settings.multipleChartsField = null;
      }
    },
  },
  mounted() {
    this.multipleCharts = this.settings && this.settings.multipleChartsField;
  },
};
</script>