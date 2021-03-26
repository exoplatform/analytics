<template>
  <v-data-table
    :headers="headers"
    :items="items"
    :items-per-page="pageSize"
    :loading="loading"
    :options.sync="options"
    hide-default-footer
    disable-pagination
    disable-filtering
    sort-desc
    class="analytics-table">
    <template
      v-for="header in headers"
      v-slot:[`item.${header.value}`]="{item}">
      <analytics-table-cell
        :key="header.value"
        :header="header"
        :item="item" />
    </template>
    <template v-if="hasMore" slot="footer">
      <v-flex class="d-flex py-2 border-box-sizing">
        <v-btn
          :loading="loading"
          :disabled="loading"
          class="btn mx-auto"
          @click="limit += pageSize">
          {{ $t('agenda.button.loadMore') }}
        </v-btn>
      </v-flex>
    </template>
  </v-data-table>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: function() {
        return null;
      },
    },
    period: {
      type: Object,
      default: function() {
        return null;
      },
    },
    selectedIdentity: {
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
    retrieveTableDataUrl: {
      type: Array,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    limit: 20,
    pageSize: 20,
    options: {},
    items: [],
    loading: false,
    sortBy: 0,
    sortDirection: 'desc',
  }),
  computed: {
    hasMore() {
      return this.limit === this.items.length;
    },
    mainFieldName() {
      return this.settings && this.settings.mainColumn && this.settings.mainColumn.aggregation && this.settings.mainColumn.aggregation.field;
    },
    mainFieldValues() {
      if (this.mainFieldName === 'userId') {
        return this.items.filter(item => item.column0 && item.column0.id).map(item => item.column0.id);
      } else if (this.mainFieldName === 'spaceId') {
        return this.items.filter(item => item.column0 && item.column0.id).map(item => item.column0.id);
      }
      return this.items.map(item => item.column0);
    },
    headers() {
      if (!this.settings) {
        return [];
      }
      const headers = [];
      if (this.settings.mainColumn) {
        this.addHeader(headers, this.settings.mainColumn, 0);
      }
      if (this.settings.columns) {
        this.settings.columns.forEach((column, index) => {
          this.addHeader(headers, column, (index +1));
        });
      }
      return headers;
    },
    labels() {
      return {
        CancelRequest: this.$t('profile.CancelRequest'),
        Confirm: this.$t('profile.Confirm'),
        Connect: this.$t('profile.Connect'),
        Ignore: this.$t('profile.Ignore'),
        RemoveConnection: this.$t('profile.RemoveConnection'),
        StatusTitle: this.$t('profile.StatusTitle'),
        join: this.$t('space.join'),
        leave: this.$t('space.leave'),
        External: this.$t('agenda.label.external'),
        Disabled: this.$t('agenda.label.disabled'),
        members: this.$t('space.members'),
      };
    },
  },
  watch: {
    selectedIdentity() {
      this.items = [];
      this.refresh();
    },
    period() {
      this.items = [];
      this.$nextTick().then(() => this.refresh());
    },
    limit() {
      this.refresh();
    },
    options: {
      handler () {
        this.items = [];
        this.refresh();
      },
      deep: true,
    },
  },
  methods: {
    addHeader(headers, column, index) {
      const dataType = column.aggregation && column.aggregation.field === 'userId' && 'userIdentity'
                      || column.aggregation && column.aggregation.field === 'spaceId' && 'spaceIdentity'
                      || column.dataType || 'text';
      headers.push({
        text: column.title || '',
        align: 'center',
        sortable: column.sortable,
        value: `column${index}`,
        class: 'text-no-wrap',
        dataType,
        column
      });
    },
    refresh() {
      if (!this.settings) {
        return;
      }
      if (this.selectedIdentity) {
        this.sortBy = 0;
      } else {
        const { sortBy, sortDesc } = this.options;

        this.sortBy = sortBy.length && this.headers.findIndex(header => header.value === sortBy[0]) || 0;
        if (this.sortBy < 0) {
          this.sortBy = 0;
        }
        this.sortDirection = sortDesc[0] && 'desc' || 'asc';
      }

      if (this.settings.mainColumn) {
        this.loading = true;
        this.refreshColumn(this.sortBy, this.limit, this.sortDirection)
          .then(() => {
            if (this.settings.columns) {
              const columnsLength = this.settings.columns.length + 1;
              for (let i  = 1; i < columnsLength; i++) {
                if (this.sortBy !== i) {
                  this.refreshColumn(i);
                }
              }
            }
          })
          .finally(() => this.loading = false);
      }
    },
    refreshColumn(columnIndex, limit, sort) {
      const column = columnIndex === 0 && this.settings.mainColumn || this.settings.columns[columnIndex-1];
      column.loading = true;
      const templateItem = {};
      this.headers.forEach(header => {
        templateItem[header.value] = 'loading';
      });

      if (columnIndex === 0 && column.aggregation && column.aggregation.field === 'userId') {
        const retrieveIdentitiesPromise = this.selectedIdentity && this.$identityService.getIdentityByProviderIdAndRemoteId(this.selectedIdentity.providerId, this.selectedIdentity.remoteId).then(identity => identity && identity.profile)
          || this.$userService.getUsers('', 0, limit, 'all');
        return retrieveIdentitiesPromise
          .then(data => {
            if (data) {
              if (this.selectedIdentity) {
                const item = Object.assign({}, templateItem);
                item[`column${columnIndex}`] = data;
                this.items.push(item);
              } else {
                data.users.forEach((user, index) => {
                  if (this.items.length <= index) {
                    const item = Object.assign({}, templateItem);
                    item[`column${columnIndex}`] = user;
                    this.items.push(item);
                  } else {
                    this.items[index][`column${columnIndex}`] = user;
                  }
                });
              }
            }
          });
      } else if (column.userField) {
        this.items.forEach(item => {
          item[`column${columnIndex}`] = item.column0 && item.column0[column.userField];
        });
      } else if (column.spaceField) {
        this.items.forEach(item => {
          item[`column${columnIndex}`] = item.column0 && item.column0[column.spaceField];
        });
      } else {
        const params = {
          lang: eXo.env.portal.language,
          column: columnIndex,
          limit: String(limit || 0),
          periodType: this.period.period || '',
          min: this.period.min,
          max: this.period.max,
        };
        if (sort) {
          params.sort = sort;
        } else if (this.mainFieldName) {
          params.fieldFilter = this.mainFieldName;
          params.fieldValues = this.mainFieldValues.join(',');
        }
        return fetch(this.retrieveTableDataUrl, {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: $.param(params),
        })
          .then((resp) => {
            if (resp && resp.ok) {
              return resp.json();
            } else {
              throw new Error('Error getting analytics with settings:', this.settings);
            }
          })
          .then((data) => {
            if (!limit) {
              this.items.forEach((item, index) => {
                item[`column${columnIndex}`] = data && data.items && data.items.find(item => item.key === this.mainFieldValues[index]);
              });
            } else if (data && data.items && data.items.length) {
              const promises = [];
              data.items.forEach((columnItem, index) => {
                const item = this.items.length <= index && Object.assign({}, templateItem) || this.items[index][`column${columnIndex}`];
                item[`column${columnIndex}`] = columnItem;
                if (this.mainFieldName === 'userId') {
                  if (columnItem.key && columnItem.key !== '0') {
                    promises.push(this.$identityService.getIdentityById(columnItem.key)
                      .then(identity => item['column0'] = identity && identity.profile)
                      .catch(() => item['column0'] = null));
                  } else {
                    item['column0'] = null;
                  }
                } else if (this.mainFieldName === 'spaceId') {
                  // TODO for spaces retrieve spaces identities
                  item['column0'] = {
                    id: columnItem.key,
                  };
                } else {
                  item['column0'] = columnItem;
                }
                if (this.items.length <= index) {
                  this.items.push(item);
                }
              });
              return Promise.all(promises);
            }
          })
          .catch((e) => {
            console.debug('fetch analytics - error', e);
            this.error = 'Error getting analytics';
            this.items.forEach(item => {
              item[`column${columnIndex}`] = 'error';
            });
          })
          .finally(() => column.loading = false);
      }
    },
  },
};
</script>