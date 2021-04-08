<template>
  <v-flex class="d-flex flex-column">
    <exo-identity-suggester
      ref="spaceSuggester"
      v-model="selectedSpaces"
      :labels="suggesterLabels"
      :multiple="multipleOperator"
      :ignore-items="spaceIds"
      include-spaces
      class="analytics-suggester"
      @input="selectIdentity" />
    <div v-if="spaces">
      <v-chip
        v-for="space in spaces"
        :key="space"
        :title="space.profile.fullName"
        color="primary"
        close
        class="identitySuggesterItem mt-2"
        @click:close="remove(space)">
        <v-avatar left>
          <v-img :src="space.profile.avatarUrl" />
        </v-avatar>
        <span class="text-truncate">
          {{ space.profile.fullName }}
        </span>
      </v-chip>
    </div>
  </v-flex>
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
    suggesterLabels: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data: () => ({
    selectedSpaces: [],
    spaces: [],
  }),
  computed: {
    operatorType() {
      return this.filter && this.filter.type;
    },
    multipleOperator() {
      return this.operatorType === 'IN_SET' || this.operatorType === 'NOT_IN_SET';
    },
    spaceIds() {
      return this.spaces.map(space => space.id);
    },
  },
  created() {
    if (this.filter.valueString) {
      const spaceIds = this.filter.valueString.split(',');
      const promises = [];
      const selectedSpaces = [];
      spaceIds.forEach(spaceId => {
        const promise = this.$spaceService.getSpaceById(spaceId)
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
              selectedSpaces.push(selectedSpace);
            }
          })
          .catch(e => console.warn('Error retrieving space with id', spaceId, e));
        promises.push(promise);
      });
      Promise.all(promises).finally(() => this.spaces = selectedSpaces);
    }
  },
  methods: {
    selectIdentity(identity) {
      this.filter.valueString = '';
      if (!this.multipleOperator) {
        this.spaces = [];
      } else {
        const selectedIdentityId = identity && (identity.length && identity[0].id || identity.id);
        if (!selectedIdentityId) {
          return;
        } else if (this.spaceIds.includes(selectedIdentityId)) {
          this.selectedSpaces = [];
          return;
        }
      }
      if (identity) {
        const identities = Array.isArray(identity) && identity || [identity];
        const identityIds = [];
        const promises = [];
        identities.forEach(identity => {
          if (identity.spaceId) {
            identityIds.push(identity.spaceId);
            this.spaces.push(identity);
            this.selectedSpaces = [];
          } else {
            const promise = this.$identityService.getIdentityByProviderIdAndRemoteId(identity.providerId, identity.remoteId)
              .then(identity => {
                if (identity.space) {
                  identityIds.push(identity.space.id);
                  this.spaces.push({
                    id: `space:${identity.space.prettyName}`,
                    providerId: 'space',
                    remoteId: identity.space.prettyName,
                    spaceId: identity.space.id,
                    profile: {
                      fullName: identity.space.displayName,
                      avatarUrl: identity.space.avatarUrl,
                    }
                  });
                }
              })
              .catch(e => console.warn('Error retrieving identity with id', identity.id, e));
            promises.push(promise);
          }
        });
        Promise.all(promises).finally(() => {
          this.filter.valueString = identityIds.join(',');
          this.selectedSpaces = [];
        });
      }
    },
    remove(space) {
      if (space && this.spaces.includes(space)) {
        this.spaces.splice(this.spaces.indexOf(space), 1);
        this.filter.valueString = this.spaces.map(space => space.id).join(',');
      }
    },
  },
};
</script>