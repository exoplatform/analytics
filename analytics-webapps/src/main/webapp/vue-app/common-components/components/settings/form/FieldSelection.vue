<template>
  <div :id="id">
    <v-autocomplete
      ref="fieldSelectAutoComplete"
      v-model="value"
      :items="fields"
      :label="label"
      :placeholder="placeholder"
      :value-comparator="selectedValueComparator"
      :return-object="false"
      :item-value="aggregation ? 'aggregationFieldName' : 'searchFieldName'"
      :attach="attach"
      :menu-props="menuProps"
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
  </div>
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
    attach: {
      type: Boolean,
      default: false,
    },
  },
  data () {
    return {
      id: `FieldSelection${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    menuProps() {
      return this.attach && 'closeOnClick, maxHeight = 100' || '';
    },
    fieldNames() {
      this.fieldsMappings.forEach(fieldMapping => {
        if (!fieldMapping.label) {
          const label = fieldMapping.name;
          const labelPart = label.indexOf('.') >= 0 ? label.substring(label.lastIndexOf('.') + 1) : label;
          const fieldLabelI18NKey = `analytics.field.label.${labelPart}`;
          const fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
          fieldMapping.label = fieldLabelI18NValue === fieldLabelI18NKey ? `${this.$t('analytics.field.label.default')} ${label}` : fieldLabelI18NValue;
        }
      });
      return this.fieldsMappings;
    },
    fields() {
      return this.fieldNames.filter(field => (!this.aggregation || field.aggregation) && (!this.numeric || field.numeric || field.date))
        .sort((a, b) => a.label.toLowerCase().localeCompare(b.label.toLowerCase()));
    },
  },
  mounted() {
    if (this.attach) {
      $(`#${this.id} input`).on('blur', () => {
        // A hack to close on select
        // See https://www.reddit.com/r/vuetifyjs/comments/819h8u/how_to_close_a_multiple_autocomplete_vselect/
        this.$refs.fieldSelectAutoComplete.isFocused = false;
      });
    }
  },
  methods: {
    updateData(){
      this.$emit('input', this.value);
      this.$emit('change', this.value);
      this.$forceUpdate();
    },
    selectedValueComparator(item1, item2){
      const item1Value = (item1 && item1.value) || item1;
      const item2Value = (item2 && item2.value) || item2;
      return item1Value === item2Value;
    },
  },
};
</script>