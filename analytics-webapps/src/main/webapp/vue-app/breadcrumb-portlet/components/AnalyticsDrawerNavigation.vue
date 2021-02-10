<template>
  <exo-drawer
    id="drawerPageAnalytics"
    ref="analyticsPage"
    right>
    <template slot="title">
      {{ $t('analytics.analyticsReports') }}
    </template>
    <template slot="content">
      <v-treeview
        :items="pages"
        item-children="children"
        item-key="uri"
        item-text="label"
        class="treeViewPage"
        open-all
        dense>
        <template slot="label" slot-scope="{ item }">
          <a
            v-if="item.link"
            class="text-color"
            :href="item.link">
            {{ item.label }}
          </a>
          <template v-else>
            {{ item.label }}
          </template>
        </template>
      </v-treeview>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    pages: {
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