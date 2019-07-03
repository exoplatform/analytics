<template>
  <v-app 
    id="AnalyticsApp"
    class="transparent"
    flat>
    <main>
      <v-card class="px-3">
        <v-card-title
          v-if="loading"
          primary-title
          class="">
          <v-progress-circular
            color="primary"
            indeterminate
            size="20" />
        </v-card-title>
    
        <v-card-title v-if="!loading && chartData" primary-title>
          <div>
            <h3 class="headline mb-0">{{ chartData.chartTitle || 'Chart title' }}</h3>
            <div v-if="chartData.dataCount"> Total samples count {{ chartData.dataCount }} </div>
            <div v-if="chartData.computingTime"> Computing time: {{ chartData.computingTime }} ms</div>
            <div v-if="searchDate"> {{ searchDate }} </div>
          </div>
        </v-card-title>
    
        <v-card-text class="px-0 mx-0">
          <analytics-line-chart
            ref="analyticsChart"
            :chart-title="chartData.chartTitle" />
        </v-card-text>
    
        <v-card-text class="px-0">
          <v-container>
            <v-layout wrap>
              <v-flex md6 sm12>
                <search-filter-form :search-filters-content="chartFilter.searchFilter.filters" />
              </v-flex>
              <v-flex md6 sm12>
                <aggregation-form :aggregations="chartFilter.aggregations" />
              </v-flex>
              <v-flex md6 sm12>
                <v-btn color="primary" @click="updateChart">
                  Update chart
                </v-btn>
              </v-flex>
            </v-layout>
          </v-container>
        </v-card-text>
      </v-card>
    </main>
  </v-app>
</template>

<script>
import AnalyticsLineChart from './chart/AnalyticsLineChart.vue';
import SearchFilterForm from './filter/SearchFilterForm.vue';
import AggregationForm from './filter/AggregationForm.vue';

export default {
  components: {
    AnalyticsLineChart,
    SearchFilterForm,
    AggregationForm,
  },
  data: () => ({
    loading: true,
    chartData: {},
    chartFilter: {
      searchFilter: {
        filters: [
          {
            field: "activityId",
            type : "EQUAL",
            valueString : "1",
          },
          {
            field: "module",
            type : "IN_SET",
            valuesString : ["social", "no_module"],
          },
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
          {
            field: "subModule",
            type : "NOT_NULL",
          },
          {
            field: "errorMessage",
            type : "IS_NULL",
          },
        ],
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
    window.setTimeout(this.updateChart, 500);
  },
  methods: {
    updateChart() {
      this.loading = true;

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
            throw new Error(`Error getting analytics of ${JSON.stringify(this.chartFilter)}`);
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
  }
};
</script>