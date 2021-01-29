<template>
  <v-app>
    <v-card
      class="border-radius"
      flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="titlePage text-color">
              <strong><a :href="DEFAULT_SITE_URI" class="labelPage">{{ $t('analytics.application') }}</a></strong>
              <a
                v-for="page in pages"
                :key="page.name"
                class="labelPage"
                :href="page.uri">
                <v-icon
                  size="16"
                  class="text-sub-title iconSubPage">
                  fa-caret-right
                </v-icon> {{ page.name }}</a>
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              v-if="listPage.length>0"
              icon
              @click="openDrawer">
              <i class="uiIconManageApplication iconAnalyticsPage"></i>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
    <analytics-drawer-navigation ref="analyticsPage" :list-page="listPage" />
  </v-app>
</template>
<script>
export default {
  data() {
    return {
      DEFAULT_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/analytics`,
      BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
      pages:[],
      listPage:[],
      allItems: [],
    };
  },
  mounted() {
    this.$analyticsUtils.getAnalyticsPages().then((analyticsPages)=>{
      const subPageAnalytics = analyticsPages.filter(site => site.name === 'analytics');
      this.listPage = subPageAnalytics.length > 0 ? subPageAnalytics[0].children : [];
      this.getAllPages();
      const uri = window.location.href.split(`${eXo.env.portal.portalName}/`)[1];
      if (uri){
        this.getPageTreeStructure(uri);
      }
    })
    this.$nextTick().then(() => this.$root.$emit('application-loaded'))
  },
  methods:{
    openDrawer(){
      this.$refs.analyticsPage.open();
    },
    getAllPages() {
      let parentNodes = [];
      let childNodes = [];
      this.allItems.push(...this.listPage);
      // init parent nodes
      parentNodes.push(...this.listPage);

      do {
        childNodes.push(...this.getChildNodes(parentNodes));
        parentNodes = [];
        parentNodes.push(...childNodes);
        childNodes = [];
      } while (parentNodes.length > 0);
    },
    getChildNodes(parentNodes) {
      // add child nodes to allItems.
      const childNodes = [];
      parentNodes.forEach(child => {
        const children = child.children;
        childNodes.push(...children);
        this.allItems.push(...children);
      });
      return childNodes;
    },
    getPageTreeStructure(uri){
      const treeStructure = uri.split('/');
      let pagePath = treeStructure[0];
      for (let i =1 ; i < treeStructure.length;i++){
        pagePath = `${pagePath}/${treeStructure[i]}`;
        const pageInfo = {
          name :this.getLabelPage(pagePath),
          uri : this.getPageUri(pagePath)
        }
        this.pages.push(pageInfo);
      }
    },
    getLabelPage(uri){
      return this.allItems.find(page =>page.uri === uri).label;
    },
    getPageUri(uri){
      return `${this.BASE_SITE_URI}${uri}`;
    },
  }
};
</script>
