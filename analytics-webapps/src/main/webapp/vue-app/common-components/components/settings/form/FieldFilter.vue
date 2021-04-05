<template>
  <v-layout class="analytics-field-filter">
    <v-flex xs3 class="my-auto">
      <analytics-field-selection
        v-model="filter.field"
        :fields-mappings="fieldsMappings"
        :placeholder="$t('analytics.fieldNamePlaceholder')"
        :attach="attach" />
    </v-flex>
    <v-flex xs4 class="d-flex ma-auto content-box-sizing">
      <select
        v-model="filter.type"
        :placeholder="$t('analytics.operatorPlaceholder')"
        :title="searchFilterTypeLabel"
        class="operatorInput border-color my-auto mx-1 text-truncate ignore-vuetify-classes"
        @change="filterTypeChanged">
        <option
          v-for="searchFilterType in searchFilterTypes"
          :key="searchFilterType.value"
          :value="searchFilterType.value"
          :title="searchFilterType.text"
          class="text-truncate">
          {{ searchFilterType.text }}
        </option>
      </select>
    </v-flex>
    <v-flex xs4 class="my-auto">
      <template v-if="isValuesComparatorType">
        <template v-if="isIdentitySuggester">
          <exo-identity-suggester
            v-show="filter.field === 'spaceId'"
            ref="spaceSuggester"
            v-model="filter.spaces"
            :labels="suggesterLabels"
            :spaces="spaces"
            :multiple="multipleOperator"
            include-spaces
            class="analytics-suggester"
            @input="selectIdentity" />
          <exo-identity-suggester
            v-show="filter.field === 'userId'"
            ref="userSuggester"
            v-model="filter.users"
            :users="users"
            :search-options="searchOptions"
            :labels="suggesterLabels"
            :multiple="multipleOperator"
            include-users
            class="analytics-suggester"
            @input="selectIdentity" />
        </template>
        <input
          v-else
          v-model="filter.valueString"
          type="text"
          :placeholder="valuesComparatorPlaceholder"
          class="ignore-vuetify-classes width-auto pa-0 my-auto"
          required>
      </template>
      <input
        v-else-if="filter.type === 'LESS' || filter.type === 'GREATER'"
        v-model.number="filter.valueString"
        type="number"
        :placeholder="$t('analytics.numberValuePlaceholder')"
        class="ignore-vuetify-classes width-auto pa-0 my-auto"
        required>
      <template v-else-if="filter.type === 'RANGE' && filter.range">
        <input
          v-model.number="filter.range.min"
          type="number"
          :placeholder="$t('analytics.minPlaceholder')"
          class="ignore-vuetify-classes width-auto pa-0 my-auto"
          required>
        <input
          v-model.number="filter.range.max"
          type="number"
          :placeholder="$t('analytics.maxPlaceholder')"
          class="ignore-vuetify-classes pa-0 width-auto my-auto"
          required>
      </template>
    </v-flex>
    <v-flex xs1 class="my-auto">
      <v-btn icon @click="$emit('delete')">
        <v-icon>fa-minus-circle</v-icon>
      </v-btn>
    </v-flex>
  </v-layout>
</template>

<script>
export default {
  props: {
    filter: {
      type: Object,
      default: function() {
        return null;
      },
    },
    fieldsMappings: {
      type: Array,
      default: function() {
        return [];
      },
    },
    attach: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    users: [],
    spaces: [],
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
    searchFilterTypes() {
      return [
        {
          text: this.$t('analytics.equals'),
          value: 'EQUAL',
          placeholder: this.$t('analytics.fieldValuePlaceholder'),
        },
        {
          text: this.$t('analytics.notEquals'),
          value: 'NOT_EQUAL',
          placeholder: this.$t('analytics.fieldValuePlaceholder'),
        },
        {
          text: this.$t('analytics.inValues'),
          value: 'IN_SET',
          placeholder: this.$t('analytics.setPlaceholder'),
        },
        {
          text: this.$t('analytics.notInValues'),
          value: 'NOT_IN_SET',
          placeholder: this.$t('analytics.setPlaceholder'),
        },
        {
          text: this.$t('analytics.inRange'),
          value: 'RANGE',
          placeholder: this.$t('analytics.rangePlaceholder'),
        },
        {
          text: this.$t('analytics.less'),
          value: 'LESS',
          placeholder: this.$t('analytics.lessPlaceholder'),
        },
        {
          text: this.$t('analytics.greater'),
          value: 'GREATER',
          placeholder: this.$t('analytics.greaterPlaceholder'),
        },
        {
          text: this.$t('analytics.isNull'),
          value: 'IS_NULL',
          placeholder: this.$t('analytics.isEmptyPlaceholder'),
        },
        {
          text: this.$t('analytics.isNotNull'),
          value: 'NOT_NULL',
          placeholder: this.$t('analytics.isNotNullPlaceholder'),
        },
      ];
    },
    filterField() {
      return this.filter && this.filter.field;
    },
    isIdentitySuggester() {
      return this.filterField === 'userId' || this.filterField === 'spaceId';
    },
    searchFilterTypesObject() {
      const searchFilterTypesObject = {};
      this.searchFilterTypes.forEach(filterType => searchFilterTypesObject[filterType.value] = filterType.text);
      return searchFilterTypesObject;
    },
    operatorType() {
      return this.filter && this.filter.type;
    },
    isValuesComparatorType() {
      return this.operatorType === 'EQUAL'
        || this.operatorType === 'NOT_EQUAL'
        || this.operatorType === 'IN_SET'
        || this.operatorType === 'NOT_IN_SET';
    },
    valuesComparatorPlaceholder() {
      return ((this.operatorType === 'EQUAL' || this.operatorType === 'NOT_EQUAL') && this.$t('analytics.valuePlaceholder'))
        || ((this.operatorType === 'IN_SET' || this.operatorType === 'NOT_IN_SET') && this.$t('analytics.setPlaceholder'));
    },
    multipleOperator() {
      return this.operatorType === 'IN_SET' || this.operatorType === 'NOT_IN_SET';
    },
    searchFilterTypeLabel() {
      return this.operatorType && this.searchFilterTypesObject[this.operatorType] || '';
    },
  },
  watch: {
    filterField() {
      this.filterTypeChanged();
    },
  },
  created() {
    if (this.isValuesComparatorType && this.isIdentitySuggester && this.filter && this.filter.valueString) {
      if (this.filterField === 'userId') {
        const identityIds = this.filter.valueString.split(',');
        const promises = [];
        const selectedUsers = [];
        identityIds.forEach(identityId => {
          const promise = this.$identityService.getIdentityById(identityId.trim())
            .then(identity => {
              if (identity && identity.profile) {
                const selectedUser = {
                  id: `${identity.providerId}:${identity.remoteId}`,
                  providerId: identity.providerId,
                  remoteId: identity.remoteId,
                  identityId: identity.id,
                  profile: {
                    fullName: identity.profile.fullname,
                    avatarUrl: identity.profile.avatar,
                  }
                };
                selectedUsers.push(selectedUser);
              }
            });
          promises.push(promise);
        });
        Promise.all(promises).then(() => {
          this.filter.users = selectedUsers;
          this.users = selectedUsers;
          this.$nextTick().then(() => this.$refs.userSuggester.init());
        });
      } else if (this.filterField === 'spaceId') {
        this.$spaceService.getSpaceById(this.filter.valueString)
          .then(space => {
            if (space) {
              const selectedSpace = {
                id: `space:${space.prettyName}`,
                providerId: 'space',
                remoteId: space.prettyName,
                spaceId: space.id,
                profile: {
                  fullName: space.displayName,
                  avatarUrl: space.avatarUrl,
                }
              };
              if (this.multipleOperator) {
                this.filter.spaces = [selectedSpace];
              } else {
                this.filter.spaces = selectedSpace;
              }
              this.spaces = [selectedSpace];
              this.$nextTick().then(() => this.$refs.spaceSuggester.init());
            }
          });
      }
    }
  },
  methods: {
    selectIdentity(identity) {
      this.filter.valueString = '';
      if (identity) {
        const identities = Array.isArray(identity) && identity || [identity];
        const identityIds = [];
        const promises = [];
        identities.forEach(identity => {
          if (identity.identityId) {
            identityIds.push(identity.identityId);
          } else if (identity.spaceId) {
            identityIds.push(identity.spaceId);
          } else {
            const promise = this.$identityService.getIdentityByProviderIdAndRemoteId(identity.providerId, identity.remoteId)
              .then(identity => {
                if (identity.space) {
                  identityIds.push(identity.space.id);
                } else if (identity.profile) {
                  identityIds.push(identity.id);
                }
              });
            promises.push(promise);
          }
        });
        Promise.all(promises).then(() => this.filter.valueString = identityIds.join(','));
      }
    },
    filterTypeChanged() {
      this.filter.valueString = '';
      this.filter.users = null;
      this.filter.spaces = null;
      this.users = [];
      this.spaces = [];
      if (this.operatorType === 'RANGE') {
        this.filter.range = {};
      } else {
        this.filter.range = null;
      }
      if (this.$refs && this.$refs.userSuggester) {
        this.$refs.userSuggester.clear();
        this.$refs.userSuggester.init();
      }
      if (this.$refs && this.$refs.spaceSuggester) {
        this.$refs.spaceSuggester.clear();
        this.$refs.spaceSuggester.init();
      }
    },
  },
};
</script>