<template>
  <v-layout>
    <v-flex>
      <v-card>
        <v-card-title
          v-if="loading"
          primary-title
          class="">
          <v-progress-circular
            color="primary"
            indeterminate
            size="20" />
        </v-card-title>

        <analytics-line-chart
          ref="analyticsChart"
          :chart-title="chartData.chartTitle" />

        <v-card-title v-if="!loading && chartData" primary-title>
          <div>
            <h3 v-if="chartData.chartTitle" class="headline mb-0">{{ chartData.chartTitle }}</h3>
            <div v-if="chartData.dataCount"> Total samples count {{ chartData.dataCount }} </div>
            <div v-if="chartData.computingTime"> Computing time: {{ chartData.computingTime }} ms</div>
            <div v-if="searchDate"> {{ searchDate }} </div>
          </div>
        </v-card-title>

        <v-card-text>
          <v-form
            @submit="
              $event.preventDefault();
              $event.stopPropagation();
            ">
            <v-container>
              <v-layout v-for="(searchFilter, index) in searchFiltersContent" :key="index">
                <v-flex md4>
                  <v-text-field
                    v-model="searchFilter.field"
                    :counter="10"
                    label="Field name"
                    required />
                </v-flex>
                <v-flex md4>
                  <v-combobox
                    v-model="searchFilter.type"
                    :items="searchFilterTypes"
                    chips
                    label="Operator"
                    required />
                </v-flex>
                <v-flex v-if="searchFilter.type === 'EQUAL' || searchFilter.type === 'LESS' || searchFilter.type === 'GREATER'" md4>
                  <v-text-field
                    v-model="searchFilter.valueString"
                    label="Value"
                    required />
                </v-flex>
                <v-flex v-else-if="searchFilter.type === 'IN_SET'" md4>
                  <v-text-field
                    v-model="searchFilter.valuesString"
                    label="Values"
                    required />
                </v-flex>
                <v-flex v-if="searchFilter.type === 'RANGE' && (searchFilter.range || (searchFilter.range = {}))" md2>
                  <v-text-field
                    v-model="searchFilter.range.min"
                    label="Min"
                    required />
                </v-flex>
                <v-flex v-if="searchFilter.type === 'RANGE' && (searchFilter.range || (searchFilter.range = {}))" md2>
                  <v-text-field
                    v-model="searchFilter.range.max"
                    label="Max"
                    required />
                </v-flex>
              </v-layout>
            </v-container>
          </v-form>
        </v-card-text>
        <v-card-text style="height: 100px; position: relative">
          <v-btn
            absolute
            dark
            fab
            top
            right
            color="pink"
            @click="addFilter">
            <v-icon>add</v-icon>
          </v-btn>
        </v-card-text>
        <v-card-text>
          <v-btn color="primary" @click="init">
            Save
          </v-btn>
        </v-card-text>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script>
import AnalyticsLineChart from './charts/AnalyticsLineChart.vue';

export default {
  components: {
    AnalyticsLineChart,
  },
  data: () => ({
    loading: true,
    chartData: {},
    searchFilterTypes: [
      {
        text: 'equals',
        value: 'EQUAL',
      },
      {
        text: 'in set of values (separator ,)',
        value: 'IN_SET',
      },
      {
        text: 'in range',
        value: 'RANGE',
      },
      {
        text: 'less or equals than',
        value: 'LESS',
      },
      {
        text: 'greater or equals than',
        value: 'GREATER',
      },
    ],
    searchFiltersContent: [
      {
        field: "activityId",
        type : "EQUAL",
        valueString : "1",
      },
//       {
//         field: "module",
//         type : "IN_SET",
//         valuesString : ["social", "no_module"],
//       },
      {
        field: "month",
        type : "RANGE",
        range : {
          min: 1,
          max: 12,
        }
      },
      {
        field: "year",
        type : "LESS",
        valueString : "2020"
      },
      {
        field: "dayOfMonth",
        type : "GREATER",
        valueString : "1"
      },
    ],
    chartFilter: {
      searchFilter: {
        filters: [],
        offset: 0,
        limit: 0
      },
      aggregations: [
        {
          type: "COUNT",
          field: "year"
        },
        {
          type: "COUNT",
          field: "month"
        },
        {
          type: "COUNT",
          field: "dayOfMonth"
        },
      ],
    },
  }),
  computed: {
    searchDate() {
      return this.chartData && this.chartData.searchDate && new Date(this.chartData.searchDate);
    },
  },
  created() {
    window.setTimeout(this.init, 500);
  },
  methods: {
    init() {
      this.loading = true;

      this.chartFilter.searchFilter.filters = this.searchFiltersContent;
      return fetch('/portal/rest/analytics', {
        method: 'POST',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.chartFilter),
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error(`Error getting analytics of ${  this.chartFilter}`);
          }
        })
        .then((chartData) => {
          this.chartData = chartData;
          this.$refs.analyticsChart.init(this.chartData.labels, this.chartData.data);
        })
        .catch((e) => {
          console.debug('fetch analytics - error', e);
          this.error = 'Error getting analytics';
        })
        .finally(() => this.loading = false);
    },
    addFilter(){
      this.searchFiltersContent.push({});
    }
  }
};
</script>