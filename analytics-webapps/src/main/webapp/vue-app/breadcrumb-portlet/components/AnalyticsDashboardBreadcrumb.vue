<template>
  <v-app>
    <v-card
      class="border-radius"
      flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="titlePage text-color">
              <a
                :href="parentAnalyticsPageUri"
                class="text-color font-weight-bold">
                {{ $t('analytics.application') }}
              </a>
              <a
                v-for="page in breadCrumbItems"
                :key="page.uri"
                :href="page.link"
                class="text-color">
                <v-icon
                  size="16"
                  class="text-sub-title iconSubPage">
                  fa-caret-right
                </v-icon>
                {{ page.label }}
              </a>
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action class="my-auto">
            <v-btn
              :title="$t('analytics.navigateToOtherPages')"
              icon
              @click="openDrawer">
              <i class="uiIconManageApplication iconAnalyticsPage text-color"></i>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
    <analytics-drawer-navigation ref="analyticsPage" :pages="subPages" />
  </v-app>
</template>
<script>
export default {
  data() {
    return {
      baseUri: eXo.env.server.portalBaseURL.replace(new RegExp(`/${eXo.env.portal.selectedNodeUri}$`), ''),
      parentAnalyticsPage: null,
    };
  },
  computed: {
    siteType() {
      return eXo.env.server.portalBaseURL.indexOf(`${eXo.env.portal.context}/g/`) === 0 && 'group' || 'portal';
    },
    groupSiteName() {
      if (this.siteType !== 'group') {
        return;
      }
      return eXo.env.server.portalBaseURL.replace(`${eXo.env.portal.context}/g/`, '').split('/')[0].replaceAll(':', '/');
    },
    siteName() {
      return this.groupSiteName || eXo.env.portal.portalName;
    },
    analyticsParentPageName() {
      if (eXo.env.portal.spaceUrl) {
        return eXo.env.portal.selectedNodeUri.split('/')[1];
      } else {
        return eXo.env.portal.selectedNodeUri.split('/')[0];
      }
    },
    parentAnalyticsPageRelativeUri() {
      if (eXo.env.portal.spaceUrl) {
        const parentPageUri = eXo.env.portal.selectedNodeUri.split('/')[0];
        return `${parentPageUri}/${this.analyticsParentPageName}`;
      } else {
        return this.analyticsParentPageName;
      }
    },
    parentAnalyticsPageUri() {
      console.log('====== the baseUri should be ===== /portal/dw');
      console.log('baseUri ',this.baseUri);
      return `${this.baseUri}/${this.parentAnalyticsPageRelativeUri}`;
    },
    subPageParts() {
      let currentUri = eXo.env.portal.selectedNodeUri.replace(this.parentAnalyticsPageRelativeUri, '');
      if (currentUri && currentUri.indexOf('/') === 0) {
        currentUri = currentUri.substring(1);
      }
      return currentUri && currentUri.split('/') || [];
    },
    subPages() {
      return this.parentAnalyticsPage && this.parentAnalyticsPage.children || [];
    },
    breadCrumbItems() {
      const breadCrumbItems = [];
      if (this.parentAnalyticsPage && this.subPageParts.length) {
        let pageIndex = this.parentAnalyticsPage;
        for (const level in this.subPageParts) {
          const pageUri = this.subPageParts[level];
          if (pageIndex && pageIndex.children) {
            const children = pageIndex.children;
            pageIndex = null;
            for (const index in children) {
              const subPageChild = children[index];
              if (subPageChild.name && subPageChild.name.toLowerCase() === pageUri.toLowerCase()) {
                pageIndex = subPageChild;
                break;
              }
            }
          }
          if (pageIndex) {
            breadCrumbItems.push(pageIndex);
          } else {
            console.error(`Can't find page with uri '${pageUri}' in pages level ${level}`);
          }
        }
      }
      return breadCrumbItems;
    },
  },
  mounted() {
    this.$analyticsUtils.getPage(this.siteType, this.siteName, this.analyticsParentPageName)
      .then(analyticsPage => {
        this.$analyticsUtils.buildPageLinkRecursively(this.baseUri, analyticsPage);
        this.parentAnalyticsPage = analyticsPage;

        // Populate HTML cache for current page breadcrumb portlet instance
        this.$nextTick().then(() => this.$root.$emit('application-loaded'));
      });
  },
  methods: {
    openDrawer(){
      this.$refs.analyticsPage.open();
    },
  }
};
</script>
