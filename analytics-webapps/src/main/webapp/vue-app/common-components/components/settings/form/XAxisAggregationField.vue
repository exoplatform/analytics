<template>
  <v-layout>
    <v-flex xs10>
      <analytics-field-selection
        v-model="aggregation.field"
        :fields-mappings="fieldsMappings"
        :label="$t('analytics.fieldName')"
        aggregation />
      <v-flex class="px-2 mt-2 d-flex flex-row">
        <v-switch
          v-model="limitResults"
          :label="$t('analytics.limitResults')"
          class="my-auto text-no-wrap" />
        <input
          v-if="limitResults"
          v-model="aggregation.limit"
          type="number"
          class="ignore-vuetify-classes inputSmall mt-1 ml-3">
        <select
          v-model="aggregation.sortDirection"
          class="ignore-vuetify-classes ml-2 mt-1 width-auto">
          <option value="desc">{{ $t('analytics.descending') }}</option>
          <option value="asc">{{ $t('analytics.ascending') }}</option>
        </select>
      </v-flex>
    </v-flex>
    <v-flex
      v-if="displayDelete"
      class="my-auto"
      xs1>
      <v-btn icon @click="$emit('delete')">
        <v-icon>fa-minus-circle</v-icon>
      </v-btn>
    </v-flex>
    <v-flex
      v-if="displayAdd"
      class="my-auto"
      xs1>
      <v-btn icon @click="$emit('add')">
        <v-icon>fa-plus-circle</v-icon>
      </v-btn>
    </v-flex>
  </v-layout>
</template>

<script>
export default {
  props: {
    aggregation: {
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
    displayAdd: {
      type: Boolean,
      default: false,
    },
    displayDelete: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    limitResults: false,
  }),
  watch: {
    limitResults() {
      if (!this.limitResults && this.aggregation) {
        this.aggregation.limit = 0;
      }
    },
  },
  mounted() {
    this.limitResults = this.aggregation && this.aggregation.limit > 0 || false;
  },
};
</script>