<template>
  <v-form
    ref="form"
    class="generalSettingForm"
    @submit="
      $event.preventDefault();
      $event.stopPropagation();
    ">
    <v-flex
      wrap
      xs12
      d-flex
      flex-column>
      <v-text-field
        v-model="settings.title"
        :label="$t('analytics.tableTitle')"
        outlined
        required />
      <h3>{{ $t('analytics.mainColumn') }}</h3>
      <v-text-field
        v-model="settings.mainColumn.title"
        :label="$t('analytics.tableMainColumnTitle')"
        required />
      <analytics-field-selection
        v-model="settings.mainColumn.aggregation.field"
        :fields-mappings="fieldsMappings"
        :label="$t('analytics.fieldName')"
        aggregation />
    </v-flex>
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
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  computed: {
    columns() {
      return this.settings && this.settings.columns || [];
    },
  },
  methods: {
    selectedValueComparator(item1, item2){
      const item1Value = (item1 && item1.value) || item1;
      const item2Value = (item2 && item2.value) || item2;
      return item1Value === item2Value;
    },
  },
};
</script>