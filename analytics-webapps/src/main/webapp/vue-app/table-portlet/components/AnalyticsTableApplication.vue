<template>
  <v-app
    :id="appId"
    class="analytics-application"
    flat>
    <div class="d-flex pa-3 white analytics-table-header" flat>
      <analytics-select-period
        v-model="selectedPeriod"
        right />
      <v-spacer />
      <exo-identity-suggester
        v-if="canUseSuggester"
        v-model="selectedIdentity"
        :search-options="searchOptions"
        :labels="suggesterLabels"
        :include-users="useUsersInSearch"
        :include-spaces="useSpacesInSearch"
        name="selectedUser"
        class="analytics-table-suggester mr-2" />
      <template v-if="canEdit">
        <v-btn
          icon
          :title="$t('analytics.settings')"
          class="d-none d-sm-inline text-header-title"
          @click="$refs.tableSettingDrawer.open()">
          <v-icon>mdi-settings</v-icon>
        </v-btn>
        <analytics-table-setting
          ref="tableSettingDrawer"
          :retrieve-mappings-url="retrieveMappingsUrl"
          :settings="settings"
          :users="userObjects"
          :spaces="spaceObjects"
          :user-fields="userFields"
          :space-fields="spaceFields"
          class="mt-0"
          @save="saveSettings" />
      </template>
    </div>
    <analytics-table
      ref="table"
      :retrieve-table-data-url="retrieveTableDataUrl"
      :settings="settings"
      :period="selectedPeriod"
      :selected-identity="selectedIdentity"
      :users="userObjects"
      :spaces="spaceObjects"
      :user-fields="userFields"
      :space-fields="spaceFields" />
  </v-app>
</template>

<script>
export default {
  props: {
    retrieveSettingsUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveMappingsUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveFiltersUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    retrieveTableDataUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
    saveSettingsUrl: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    settings: null,
    appId: `AnalyticsApplication${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    loading: true,
    canEdit: false,
    selectedIdentity: null,
    error: null,
    scope: null,
    initialized: false,
    selectedPeriod: null,
    userObjects: {},
    spaceObjects: {},
    columnsData: {},
    searchOptions: {
      currentUser: '',
    },
  }),
  computed: {
    suggesterLabels() {
      return {
        searchPlaceholder: this.$t('analytics.searchPlaceholder'),
        placeholder: this.$t('analytics.searchPlaceholder'),
        noDataLabel: this.$t('analytics.noDataLabel'),
      };
    },
    mainColumnAggregation() {
      return this.settings && this.settings.mainColumn && this.settings.mainColumn.valueAggregation && this.settings.mainColumn.valueAggregation.aggregation;
    },
    mainColumnAggregationField() {
      return this.mainColumnAggregation && this.mainColumnAggregation.field;
    },
    useUsersInSearch() {
      return this.mainColumnAggregationField === 'userId';
    },
    useSpacesInSearch() {
      return this.mainColumnAggregationField === 'spaceId';
    },
    canUseSuggester() {
      return this.useUsersInSearch || this.useSpacesInSearch;
    },
    scopeTooltip() {
      switch (this.scope) {
      case 'NONE': return this.$t('analytics.permissionDenied');
      case 'GLOBAL': return this.$t('analytics.noDataRestriction');
      case 'USER': return this.$t('analytics.dataRestrictedToCurrentUser');
      case 'SPACE': return this.$t('analytics.dataRestrictedToCurrentSpace');
      }
      return this.error;
    },
    userFields() {
      return [{
        name: 'profession',
        label: this.$t('analytics.profession'),
        sortable: false,
        type: 'string',
      }, {
        name: 'team',
        label: this.$t('analytics.team'),
        sortable: false,
        type: 'string',
      }, {
        name: 'enabled',
        label: this.$t('analytics.enabled'),
        sortable: true,
        type: 'boolean',
      }, {
        name: 'spacesCount',
        label: this.$t('analytics.spacesCount'),
        sortable: false,
        type: 'long',
      }, {
        name: 'createdDate',
        label: this.$t('analytics.createdDate'),
        sortable: true,
        type: 'date',
      }, {
        name: 'enrollmentDate',
        label: this.$t('analytics.enrollmentDate'),
        sortable: true,
        type: 'date',
      }, {
        name: 'lastUpdatedTime',
        label: this.$t('analytics.lastUpdatedTime'),
        sortable: true,
        type: 'date',
      }, {
        name: 'lastLoginTime',
        label: this.$t('analytics.lastLoginTime'),
        sortable: true,
        type: 'date',
      }];
    },
    spaceFields() {
      return [{
        name: 'createdTime',
        label: this.$t('analytics.createdDate'),
        sortable: true,
        type: 'date',
      }, {
        name: 'managersCount',
        label: this.$t('analytics.managersCount'),
        sortable: true,
        type: 'long',
      }, {
        name: 'membersCount',
        label: this.$t('analytics.membersCount'),
        sortable: true,
        type: 'long',
      }, {
        name: 'redactorsCount',
        label: this.$t('analytics.redactorsCount'),
        sortable: true,
        type: 'long',
      }, {
        name: 'template',
        label: this.$t('analytics.template'),
        sortable: true,
        type: 'string',
      }, {
        name: 'subscription',
        label: this.$t('analytics.subscription'),
        sortable: false,
        type: 'string',
      }, {
        name: 'visibility',
        label: this.$t('analytics.visibility'),
        sortable: false,
        type: 'string',
      }];
    },
  },
  watch: {
    selectedPeriod(newValue, oldValue) {
      if (!oldValue && newValue && !this.initialized) {
        this.initialized = true;
        this.init();
      }
    },
  },
  methods: {
    init() {
      this.loading = true;
      return this.getSettings()
        .then(this.$nextTick)
        .then(this.getFilters)
        .then(this.$nextTick)
        .then(this.updateTable)
        .finally(() => {
          this.loading = false;
        });
    },
    getSettings() {
      return fetch(this.retrieveSettingsUrl, {
        method: 'GET',
        credentials: 'include',
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error(`Error getting analytics of table '${this.title}'`);
          }
        })
        .then((settings) => {
          this.scope = settings && settings.scope;
          this.canEdit = settings && settings.canEdit;
          this.title = settings && settings.title || '';
        })
        .catch((e) => {
          console.warn('Error retrieving table settings', e);
          this.error = 'Error retrieving table settings';
        });
    },
    getFilters() {
      return fetch(this.retrieveFiltersUrl, {
        method: 'GET',
        credentials: 'include',
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error(`Error getting analytics of ${JSON.stringify(this.settings)}`);
          }
        })
        .then((settings) => {
          if (!settings) {
            settings = {};
          }
          if (!settings.mainColumn) {
            settings.mainColumn = {
              title: settings.mainColumn.title || '',
              valueAggregation: {
                
              },
              thresholdAggregation: {
                
              },
              aggregation: {
                field: settings.mainColumn.field || null,
                type: settings.mainColumn.type || 'TERMS',
              },
            };
          }
          if (!settings.columns) {
            settings.columns = [];
          }
          this.settings = settings;
        })
        .catch((e) => {
          console.warn('Error retrieving table filters', e);
          this.error = 'Error retrieving table filters';
        });
    },
    saveSettings(settings) {
      this.loading = true;

      const settingsToSave = JSON.parse(JSON.stringify(settings));
      // Cleanup saved settings
      settingsToSave.columns.forEach(column => {
        if (!column.valueAggregation || !column.valueAggregation.aggregation || !column.valueAggregation.aggregation.type) {
          delete column.valueAggregation;
        }
        if (!column.thresholdAggregation || !column.thresholdAggregation.aggregation || !column.thresholdAggregation.aggregation.type) {
          delete column.thresholdAggregation;
        }
      });
      if (!settingsToSave.mainColumn.valueAggregation || !settingsToSave.mainColumn.valueAggregation.aggregation || !settingsToSave.mainColumn.valueAggregation.aggregation.type) {
        delete settingsToSave.mainColumn.valueAggregation;
      }

      return fetch(this.saveSettingsUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: $.param({
          settings : JSON.stringify(settingsToSave)
        }),
      })
        .then((resp) => {
          if (!resp || !resp.ok) {
            throw new Error('Error saving table settings', settingsToSave);
          }

          return this.init();
        })
        .catch((e) => {
          console.warn('Error saving table settings', e);
          this.error = 'Error saving table settings';
        })
        .finally(() => {
          this.loading = false;
        });
    },
    updateTable() {
      if (!this.selectedPeriod) {
        return;
      }
      this.$refs.table.refresh();
    },
  }
};
</script>