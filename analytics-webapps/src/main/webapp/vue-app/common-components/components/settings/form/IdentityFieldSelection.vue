<template>
  <div :id="id">
    <v-autocomplete
      ref="fieldSelectAutoComplete"
      v-model="value"
      :items="items"
      :label="label"
      :value-comparator="selectedValueComparator"
      :return-object="false"
      menu-props="closeOnClick, maxHeight = 100"
      item-text="label"
      item-value="name"
      return-value
      class="pt-0 pb-3"
      filled
      attach
      persistent-hint
      dense
      chips
      @input="updateData" />
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: function() {
        return null;
      },
    },
    items: {
      type: Array,
      default: function() {
        return [];
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
      id: `FieldSelection${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
    };
  },
  mounted() {
    $(`#${this.id} input`).on('blur', () => {
      // A hack to close on select
      // See https://www.reddit.com/r/vuetifyjs/comments/819h8u/how_to_close_a_multiple_autocomplete_vselect/
      this.$refs.fieldSelectAutoComplete.isFocused = false;
    });
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