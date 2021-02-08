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
            :href="item.trueUri">{{ item.label }}</a>
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
  methods:{
    open(){
      this.$refs.analyticsPage.open();
    },
    close(){
      this.$refs.analyticsPage.close();
    },
  }
};
</script>