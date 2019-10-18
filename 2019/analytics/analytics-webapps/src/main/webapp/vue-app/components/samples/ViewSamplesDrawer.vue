<template>
  <v-navigation-drawer
    v-model="drawer"
    :attach="`#${parentId}`"
    fixed
    right
    temporary
    width="700"
    max-width="100vw"
    class="samplesDrawer">
    <v-toolbar>
      <v-toolbar-title>
        Data of chart
        <span class="primary--text">{{ chartSettings && chartSettings.title }}</span>
      </v-toolbar-title>
      <v-spacer />
      <v-btn
        icon
        color="secondary"
        @click="drawer = false">
        <v-icon>
          close
        </v-icon>
      </v-btn>
    </v-toolbar>
    <v-row justify="center">
      <v-expansion-panels v-if="chartDatas" accordion>
        <sample-item
          v-for="(chartData, i) in chartDatas"
          :key="i"
          :chart-data="chartData"
          :users="userObjects"
          :spaces="spaceObjects" />
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

export default {
  components: {
    SampleItem,
  },
  props: {
    parentId: {
      type: String,
      default: function() {
        return null;
      },
    },
    settings: {
      type: Object,
      default: function() {
        return null;
      },
    },
    selectedPeriod: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    chartDatas: null,
    userObjects: {},
    spaceObjects: {},
    pageSize: 10,
    limit: 10,
  }),
  computed: {
    canLoadMore() {
      if (!this.chartDatas) {
        return false;
      }
      const loadedDataLength = Object.keys(this.chartDatas).length;
      return loadedDataLength % this.pageSize === 0 || loadedDataLength < this.limit;
    },
    chartSettings() {
      return Object.assign({}, this.settings);
    }
  },
  watch: {
    drawer() {
      if (this.drawer && !this.chartDatas) {
        this.loadData();
      }
      if (this.drawer) {
        $('body').addClass('hide-scroll');
      } else {
        $('body').removeClass('hide-scroll');
      }
    },
  },
  methods: {
    open() {
      this.drawer = true;
    },
    loadMore() {
      this.limit += this.pageSize;
      this.loadData();
    },
    loadData() {
      this.loading = true;

      const filters = this.chartSettings.filters.slice();
      if (this.selectedPeriod) {
        filters.push({
          field: "timestamp",
          type: "RANGE",
          range: this.selectedPeriod,
        });
      }

      let loadedChartData;
      return fetch('/portal/rest/analytics', {
        method: 'POST',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          filters: filters,
          offset: 0,
          limit: this.limit,
          lang: eXo.env.portal.language,
        }),
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error('Error getting analytics with filters:', filters);
          }
        })
        .then((chartDatas) => loadedChartData = chartDatas)
        .then((chartDatas) => this.loadUsersAndSpacesObjects(chartDatas))
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
        return this.loadUser(chartData.userId)
          .then(() => this.loadUser(chartData.parameters && chartData.parameters.modifierSocialId))
          .then(() => this.loadSpace(chartData.spaceId))
          .then(() => this.loadUsersAndSpacesObjects(chartDatas, ++index));
      }
    },
    loadUser(userId) {
      if (this.userObjects[userId]) {
        return Promise.resolve(this.userObjects[userId]);
      } else {
        return fetch(`/portal/rest/v1/social/identities/${userId}`)
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          }
        })
        .then((obj) => {
          obj = obj || {};
          const userObject = {
            identityId: userId,
            providerId: obj.globalId && obj.globalId.domain,
            remoteId: obj.globalId && obj.globalId.localId,
            avatar: obj.avatar || '/eXoSkin/skin/images/system/UserAvtDefault.png',
            displayName: obj.profile && obj.profile.fullname,
          };
          this.userObjects[userId] = userObject;
        })
      }
    },
    loadSpace(spaceId) {
      if (this.spaceObjects[spaceId]) {
        return Promise.resolve(this.spaceObjects[spaceId]);
      } else {
        let spaceDisplayName;
        return fetch(`/portal/rest/v1/social/spaces/${spaceId}`)
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          }
        })
        .then(obj => {
          if (obj && obj.identity) {
            spaceDisplayName = obj.displayName;
            return fetch(`${obj.identity}`)
              .then((resp) => resp && resp.ok && resp.json());
          }
        })
        .then((obj) => {
          obj = obj || {};
          const spaceObject = {
            identityId: obj.id,
            providerId: obj.globalId && obj.globalId.domain,
            remoteId: obj.globalId && obj.globalId.localId,
            avatar: obj.avatar || '/eXoSkin/skin/images/system/SpaceAvtDefault.png',
            spaceId: spaceId,
            displayName: spaceDisplayName,
          };
          this.spaceObjects[spaceId] = spaceObject;
        })
      }
    },
  }
}
</script>