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
        <v-flex class="my-auto px-2" xs12>
          <v-switch
            v-model="dateAggregationType"
            label="Per date"
            required
            @change="$forceUpdate()" />
        </v-flex>
        <template v-if="dateAggregationType">
          <v-flex class="my-auto px-2" xs6>
            <v-select
              v-model="dateAggregation.interval"
              :items="dateFields"
              :item-text="text"
              :item-value="value"
              :value-comparator="selectedValueComparator"
              label="Date type"
              class="operatorInput"
              hide-selected
              persistent-hint
              chips
              @change="$forceUpdate()" />
          </v-flex>
        </template>
        <template v-else>
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
  data: () => ({
    dateAggregationType: true,
    dateFields: [
      {
        text: 'Day',
        value: 'day',
      },
      {
        text: 'Week',
        value: 'week',
      },
      {
        text: 'Month',
        value: 'month',
      },
      {
        text: 'Quarter',
        value: 'quarter',
      },
      {
        text: 'Year',
        value: 'year',
      },
    ],
    dateAggregation: {
      type: 'DATE',
      field: 'timestamp',
      sortDirection: 'asc',
      interval: 'month',
    },
  }),
  computed: {
    xAxisAggregations() {
      return this.settings && this.settings.xAxisAggregations;
    },
  },
  watch: {
    dateAggregationType() {
      if (this.dateAggregationType) {
        this.settings.xAxisAggregations = [this.dateAggregation];
      } else {
        this.settings.xAxisAggregations = [];
      }
    }
  },
  mounted() {
    this.dateAggregationType = this.xAxisAggregations && this.xAxisAggregations.length === 1 && this.xAxisAggregations[0].type === 'DATE';
  },
  methods: {
    deleteAggregation(aggregationIndex) {
      this.xAxisAggregations.splice(aggregationIndex, 1);
    },
    addAggregation() {
      this.xAxisAggregations.push({type: this.dateAggregationType ? 'DATE' : 'COUNT'});
      this.$forceUpdate();
    },
  },
};
</script>