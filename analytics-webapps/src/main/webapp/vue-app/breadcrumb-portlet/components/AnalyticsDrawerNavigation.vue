<template>
  <exo-drawer
    id="drawerPageAnalytics"
    ref="analyticsPage"
    right>
    <template slot="title">
      {{ $t('analytics.drawer') }}
    </template>
    <template slot="content">
      <v-treeview
        class="treeViewPage"
        activatable
        dense
        hoverable
        :items="listPage">
        <template slot="label" slot-scope="{ item }">
          <a
            class="pageNameAnalytics"
            :href="getPageUri(item.uri)">{{ item.label }}</a>
        </template>
      </v-treeview>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    listPage: {
      type: Object,
      default: () => [],
    },
  },
  data() {
    return {
      BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
    };
  },
  methods:{
    getPageUri(uri){
      return `${this.BASE_SITE_URI}${uri}`;
    },
    open(){
      this.$refs.analyticsPage.open();
    },
    close(){
      this.$refs.analyticsPage.close();
    },
  }
};
</script>