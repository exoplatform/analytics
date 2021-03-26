<template>
  <v-dialog
    id="analyticsTableSettingsModal"
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="editChatSettings"
    width="750px"
    max-width="100vw"
    @keydown.esc="dialog = false">
    <v-card v-if="dialog" class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="dialog = false">
        </a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle">
          {{ $t('analytics.settings') }}
        </span>
      </div>
      <v-card-text v-if="tableSettings">
        <v-text-field
          v-model="tableSettings.title"
          :label="$t('analytics.tableTitle')"
          :placeholder="$t('analytics.tableTitlePlaceholder')"
          outlined
          class="mt-0 pt-2" />
        <h3>{{ $t('analytics.mainColumn') }}</h3>
        <v-text-field
          v-model="tableSettings.mainColumn.title"
          :label="$t('analytics.tableMainColumnTitle')"
          :placeholder="$t('analytics.tableMainColumnTitlePlaceholder')"
          outlined
          required />
        <analytics-field-selection
          v-model="tableSettings.mainColumn.aggregation.field"
          :fields-mappings="fieldsMappings"
          :label="$t('analytics.fieldName')"
          aggregation />
        <v-list>
          <v-subheader class="px-0">
            <h3>{{ $t('analytics.tableColumns') }}</h3>
            <v-btn
              color="primary"
              icon
              @click="addColumn">
              <v-icon>fa-plus-circle</v-icon>
            </v-btn>
          </v-subheader>
          <analytics-table-column-setting
            v-for="(column, index) in columns"
            :key="index"
            :fields-mappings="fieldsMappings"
            :main-column="mainColumn"
            :column="column"
            :user-fields="userFields"
            :space-fields="spaceFields"
            @delete="deleteColumn(index)" />
        </v-list>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <button class="btn btn-primary ignore-vuetify-classes mr-1" @click="save">
          {{ $t('analytics.save') }}
        </button>
        <button class="btn ignore-vuetify-classes ml-1" @click="dialog = false">
          {{ $t('analytics.close') }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    retrieveMappingsUrl: {
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
    userFields: {
      type: Array,
      default: function() {
        return null;
      },
    },
    spaceFields: {
      type: Array,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      tableSettings: null,
      fieldsMappings: [],
      dialog: false,
      tab: 0,
    };
  },
  computed: {
    columns() {
      return this.tableSettings && this.tableSettings.columns || [];
    },
    mainColumn() {
      return this.tableSettings && this.tableSettings.mainColumn;
    },
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.init();
      }
    },
  },
  methods: {
    init() {
      this.loading = true;

      return fetch(this.retrieveMappingsUrl, {
        method: 'GET',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
        },
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.json();
          } else {
            throw new Error('Error getting analytics fields mappings:');
          }
        })
        .then((fieldsMappings) => {
          this.fieldsMappings = fieldsMappings;
          return this.$nextTick();
        })
        .then(() => {
          if (this.$refs) {
            Object.keys(this.$refs).forEach(refKey => {
              const component = this.$refs[refKey];
              if (component && component.init) {
                component.init();
              }
            });
          }
        })
        .catch((e) => {
          console.debug('fetch analytics - error', e);
          this.error = 'Error getting analytics';
        })
        .finally(() => this.loading = false);
    },
    addColumn() {
      this.columns.push({
        aggregation: {
          type: 'COUNT',
        },
        filter: [],
      });
    },
    deleteColumn(index) {
      this.columns.splice(index, 1);
    },
    open() {
      const settings = this.settings || {};
      const tableSettings = {
        title: settings.title || '',
        mainColumn: {
          title: '',
          aggregation: {},
        },
        columns: [],
      };
      if (settings.mainColumn) {
        if (settings.mainColumn.title) {
          tableSettings.mainColumn.title = settings.mainColumn.title;
        }
        if (settings.mainColumn.aggregation) {
          tableSettings.mainColumn.aggregation = {
            field: settings.mainColumn.aggregation.field || null,
            type: settings.mainColumn.type || 'TERMS',
            dataType: settings.mainColumn.dataType || 'text',
          };
        }
      }
      if (settings.columns && settings.columns.length) {
        settings.columns.forEach(column => {
          tableSettings.columns.push({
            title: column.title || '',
            previousPeriod: column.previousPeriod || false,
            userField: column.userField || '',
            spaceField: column.spaceField || '',
            aggregation: column.aggregation || {},
            filters: column.filters || [],
            sortable: column.sortable || false,
            dataType: column.dataType || 'text',
          });
        });
      }
      this.tableSettings = tableSettings;
      this.dialog = true;
    },
    save() {
      this.$emit('save', this.tableSettings);
      this.dialog = false;
    },
  },
};
</script>
