<template>
  <v-form
    ref="form"
    class="aggregationForm"
    @submit="
      $event.preventDefault();
      $event.stopPropagation();
    ">
    <v-card-text>
      <template v-if="xAxisAggregations && xAxisAggregations.length">
        <v-layout v-for="(item, index) in xAxisAggregations" :key="index">
          <v-flex xs10>
            <v-text-field
              v-model="item.field"
              label="Field name"
              required />
          </v-flex>
          <v-flex class="my-auto" xs1>
            <v-btn icon @click="deleteAggregation(index)">
              <v-icon>fa-minus-circle</v-icon>
            </v-btn>
          </v-flex>
          <v-flex
            v-if="(index +1) === xAxisAggregations.length"
            class="my-auto"
            xs1>
            <v-btn icon @click="addAggregation">
              <v-icon>fa-plus-circle</v-icon>
            </v-btn>
          </v-flex>
        </v-layout>
      </template>
      <v-layout v-else>
        <v-flex class="my-auto" xs1>
          <v-btn icon @click="addAggregation">
            <v-icon>fa-plus-circle</v-icon>
          </v-btn>
        </v-flex>
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
  },
  computed: {
    xAxisAggregations() {
      return this.settings && this.settings.xAxisAggregations;
    },
  },
  methods: {
    deleteAggregation(aggregationIndex) {
      this.xAxisAggregations.splice(aggregationIndex, 1);
    },
    addAggregation() {
      this.xAxisAggregations.push({type: 'COUNT'});
      this.$forceUpdate();
    },
  },
};
</script>