<template>
  <v-autocomplete
    v-model="value"
    :items="fields"
    :label="label"
    :placeholder="placeholder"
    :value-comparator="selectedValueComparator"
    :return-object="false"
    :item-value="aggregation ? 'aggregationFieldName' : 'searchFieldName'"
    item-text="label"
    class="operatorInput pa-0"
    filled
    persistent-hint
    chips
    dense
    @change="updateData">
    <template v-slot:selection="data">
      <v-chip
        v-bind="data.attrs"
        :input-value="data.selected"
        @click="data.select">
        {{ data.item && data.item.label || data.item }}
      </v-chip>
    </template>
    <template v-slot:item="data">
      <v-list-item-content v-text="data.item.label" />
    </template>
  </v-autocomplete>
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
    placeholder: {
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
        let label = null;
        if (!fieldMapping.label && this.fieldsMappings[fieldMapping.name]) {
          label = this.fieldLabels[fieldMapping.name];
        } else {
          label = fieldMapping.name;
        }
        const fieldLabelI18NKey = `analytics.field.label.${label}`;
        const fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
        fieldMapping.label = fieldLabelI18NValue === fieldLabelI18NKey ? label : fieldLabelI18NValue;
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