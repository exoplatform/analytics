<template>
  <v-navigation-drawer
    v-model="drawer"
    absolute
    right
    stateless
    temporary
    width="700"
    max-width="100vw"
    class="samplesDrawer">
    <v-toolbar color="blue lighten-5">
      <v-toolbar-title>
        Data of chart
        <span class="primary--text">{{ title }}</span>
      </v-toolbar-title>
      <v-spacer />
      <v-btn
        :loading="loading"
        :disabled="loading"
        icon
        color="primary"
        class="mx-2"
        @click="refresh">
        <v-icon>
          refresh
        </v-icon>
      </v-btn>
      <v-btn
        icon
        color="secondary"
        @click="drawer = false">
        <v-icon>
          close
        </v-icon>
      </v-btn>
    </v-toolbar>
    <v-row justify="center" class="ma-0 analyticsDrawerContent">
      <v-expansion-panels v-if="chartDatas" accordion>
        <sample-item
          v-for="chartData in chartDatas"
          :key="chartData.id"
          :chart-data="chartData"
          :users="users"
          :spaces="spaces" />
      </v-expansion-panels>
      <v-progress-circular
        v-else
        color="primary"
        indeterminate
        size="20" />
    </v-row>
    <v-row v-if="canLoadMore" justify="center">
      <v-btn
        :loading="loading"
        :disabled="loading"
        color="primary"
        text
        @click="loadMore">
        Load More
      </v-btn>
    </v-row>
  </v-navigation-drawer>
</template>

<script>
import SampleItem from './SampleItem.vue';

import {loadUser, loadSpace} from '../../js/utils.js';

export default {
  components: {
    SampleItem,
  },
  props: {
    selectedPeriod: {
      type: Object,
      default: function() {
        return null;
      },
    },
    title: {
      type: String,
      default: function() {
        return null;
      },
    },
    users: {
      type: Object,
      default: function() {
        return null;
      },
    },
    spaces: {
      type: Object,
      default: function() {
        return null;
      },
    },
    retrieveSamplesUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    chartDatas: null,
    pageSize: 10,
    limit: 10,
  }),
  computed: {
    canLoadMore() {
      if (!this.chartDatas) {
        return false;
      }
      const loadedDataLength = Object.keys(this.chartDatas).length;
      return loadedDataLength % this.pageSize === 0;
    },
  },
  watch: {
    drawer() {
      if (this.drawer) {
        $('body').addClass('hide-scroll');
        this.loadData();

        this.$nextTick().then(() => {
          $('.analytics-application .v-overlay').click(() => {
            this.drawer = false;
          });
        });
      } else {
        $('body').removeClass('hide-scroll');
      }
    },
  },
  created() {
    $(document).on('keydown', (event) => {
      if (event && event.key === 'Escape') {
        this.drawer = false;
      }
    });
  },
  methods: {
    open() {
      this.drawer = true;
    },
    loadMore() {
      this.limit += this.pageSize;
      this.selectedPeriod.max = Date.now();
      this.loadData();
    },
    refresh() {
      this.selectedPeriod.max = Date.now();
      this.loadData();
    },
    loadData() {
      if (!this.selectedPeriod) {
        return;
      }

      let loadedChartData;
      const params = {
        lang: eXo.env.portal.language,
        min: this.selectedPeriod.min,
        max: this.selectedPeriod.max,
        limit: this.limit,
      };

      this.loading = true;
      return fetch(this.retrieveSamplesUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'pragma': 'no-cache',
          'cache-control': 'no-cache',
        },
        body: $.param(params),
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error('Error getting analytics samples with filters:', params);
          }
        })
        .then((chartDatas) => loadedChartData = chartDatas)
        .then((chartDatas) => this.loadUsersAndSpacesObjects(chartDatas))
        .then(() => this.chartDatas = [])
        .then(() => this.$nextTick())
        .then(() => this.chartDatas = loadedChartData)
        .catch((e) => {
          console.debug('fetch analytics - error', e);
          this.error = 'Error getting analytics';
        })
        .finally(() => this.loading = false);
    },
    loadUsersAndSpacesObjects(chartDatas, index) {
      index = index || 0;
      if (!chartDatas || index >= chartDatas.length) {
        return;
      }
      const chartData = chartDatas[index];
      if (chartData) {
        return loadUser(this.users, chartData.userId)
          .then(() => loadUser(this.users, chartData.parameters && chartData.parameters.modifierSocialId))
          .then(() => loadSpace(this.spaces, chartData.spaceId))
          .then(() => this.loadUsersAndSpacesObjects(chartDatas, ++index));
      }
    },
  }
}
</script>