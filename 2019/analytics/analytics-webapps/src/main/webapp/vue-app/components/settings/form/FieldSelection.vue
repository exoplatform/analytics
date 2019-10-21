<template>
  <v-select
    v-model="value"
    :items="fields"
    :label="label"
    :value-comparator="selectedValueComparator"
    :return-object="false"
    item-text="label"
    :item-value="aggregation ? 'aggregationFieldName' : 'searchFieldName'"
    class="operatorInput"
    persistent-hint
    chips
    @change="updateData" />
</template>

<script>
export default {
  props: {
    fieldsMappings: {
      type: Array,
      default: function() {
        return [];
      },
    },
    aggregation: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    numeric: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    value: {
      type: String,
      default: function() {
        return null;
      },
    },
    label: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data () {
    return {
      fieldLabels: {
        module: 'Module',
        subModule: 'Sub-module',
        operation: 'Operation',
        status: 'Status code',
        errorCode: 'Error code',
        errorMessage: 'Error message',
        year: 'Year',
        month: 'Month',
        week: 'Week',
        dayOfMonth: 'Day of month',
        dayOfWeek: 'Day of week',
        dayOfYear: 'Day of year',
        hour: 'Hour of day',
        userId: 'User',
        spaceId: 'Space',
      },
    };
  },
  computed: {
    fieldNames() {
      this.fieldsMappings.forEach(fieldMapping => {
        if (!fieldMapping.label && this.fieldsMappings[fieldMapping.name]) {
          fieldMapping.label = this.fieldLabels[fieldMapping.name];
        } else {
          fieldMapping.label = fieldMapping.name;
        }
      });
      return this.fieldsMappings;
    },
    fields() {
      return this.fieldNames.filter(field => (!this.aggregation || field.aggregation) && (!this.numeric || field.numeric));
    },
  },
  methods: {
    updateData(){
      this.$emit('input', this.value);
      this.$forceUpdate();
    },
    selectedValueComparator(item1, item2){
      const item1Value = (item1 && item1.value) || item1;
      const item2Value = (item2 && item2.value) || item2;
      return item1Value === item2Value;
    },
  },
}
</script>