<template>
  <div>
    <v-btn
      link
      text
      class="primary--text"
      @click="$refs.multipleValuesDrawer.open()">
      {{ $t('analytics.openItemsList') }}
    </v-btn>
    <exo-drawer
      ref="multipleValuesDrawer"
      drawer-width="650px"
      right
      @closed="$emit('closed')">
      <template slot="content">
        <v-flex class="mx-0 drawerHeader flex-grow-0">
          <v-list-item
            class="pr-0"
            :ripple="false"
            :selectable="false"
            inactive>
            <v-list-item-action class="drawerIcons align-end d-flex flex-row">
              <v-btn icon>
                <v-icon @click="$refs.multipleValuesDrawer.close()">mdi-arrow-left</v-icon>
              </v-btn>
            </v-list-item-action>
            <v-list-item-content class="drawerTitle align-start text-header-title text-truncate">
              {{ fieldLabel }}
            </v-list-item-content>
          </v-list-item>
        </v-flex>
        <v-divider class="my-0" />
        <v-flex class="pa-5">
          <analytics-space-field-filter
            v-if="filterField === 'spaceId'"
            :filter="filter"
            :suggester-labels="suggesterLabels" />
          <analytics-user-field-filter
            v-else-if="filterField === 'userId'"
            :filter="filter"
            :suggester-labels="suggesterLabels" />
          <analytics-text-value-filter
            v-else-if="aggregation"
            :filter="filter"
            :suggester-labels="suggesterLabels" />
          <input
            v-else
            v-model="filter.valueString"
            type="text"
            :placeholder="placeholder"
            class="ignore-vuetify-classes width-auto pa-0 my-auto"
            required>
        </v-flex>
      </template>
    </exo-drawer>
  </div>
</template>

<script>
export default {
  props: {
    fieldLabel: {
      type: Object,
      default: function() {
        return null;
      },
    },
    filter: {
      type: Object,
      default: function() {
        return null;
      },
    },
    suggesterLabels: {
      type: Object,
      default: function() {
        return null;
      },
    },
    placeholder: {
      type: String,
      default: null,
    },
    aggregation: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    filterField() {
      return this.filter && this.filter.field;
    },
  },
};
</script>