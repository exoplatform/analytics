<template>
  <v-select
    v-model="value"
    :items="fields"
    :label="label"
    :value-comparator="selectedValueComparator"
    :return-object="false"
    item-text="text"
    item-value="value"
    class="operatorInput"
    hide-selected
    persistent-hint
    chips
    @change="updateData" />
</template>

<script>
export default {
  props: {
    aggregation: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    number: {
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
      fieldNames:[
        {
          text: 'Module',
          value: 'module',
          aggregation: true,
        },
        {
          text: 'Sub-module',
          value: 'subModule',
          aggregation: true,
        },
        {
          text: 'Operation',
          value: 'operation',
          aggregation: true,
        },
        {
          text: 'Status code',
          value: 'status',
          aggregation: true,
          number: true,
        },
        {
          text: 'Error code',
          value: 'errorCode',
          aggregation: true,
          number: true,
        },
        {
          text: 'Error message',
          value: 'errorMessage',
          aggregation: false,
        },
        {
          text: 'Year',
          value: 'year',
          aggregation: true,
          number: true,
        },
        {
          text: 'Month',
          value: 'month',
          aggregation: true,
          number: true,
        },
        {
          text: 'Week',
          value: 'week',
          aggregation: true,
          number: true,
        },
        {
          text: 'Day of month',
          value: 'dayOfMonth',
          aggregation: true,
          number: true,
        },
        {
          text: 'Day of week',
          value: 'dayOfWeek',
          aggregation: true,
          number: true,
        },
        {
          text: 'Day of year',
          value: 'dayOfYear',
          aggregation: true,
          number: true,
        },
        {
          text: 'Hour of day',
          value: 'hour',
          aggregation: true,
          number: true,
        },
        {
          text: 'User social id',
          value: 'userId',
          aggregation: true,
          number: true,
        },
        {
          text: 'Space id',
          value: 'spaceId',
          aggregation: true,
          number: true,
        },
      ],
    };
  },
  computed: {
    fields() {
      return this.fieldNames.filter(field => (!this.aggregation || field.aggregation) && (!this.number || field.number));
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