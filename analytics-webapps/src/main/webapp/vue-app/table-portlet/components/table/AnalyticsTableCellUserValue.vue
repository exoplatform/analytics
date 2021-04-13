<template>
  <v-progress-circular
    v-if="loading"
    size="24"
    color="primary"
    indeterminate />
  <v-tooltip v-else-if="error" bottom>
    <template v-slot:activator="{ on, attrs }">
      <i
        class="uiIconColorError"
        v-bind="attrs"
        v-on="on"></i>
    </template>
    <span>{{ $t('analytics.errorRetrievingDataForValue', {0: value}) }}</span>
  </v-tooltip>
  <exo-user-avatar
    v-else-if="user"
    :title="user.fullname"
    :username="user.username"
    :fullname="user.fullname"
    :url="`/portal/dw/profile/${user.username}`"
    :size="32"
    :avatar-url="user.avatar"
    :labels="labels"
    :external="user.external"
    :retrieve-extra-information="false"
    class="analytics-table-user my-1"
    avatar-class="border-color">
    <template v-if="user.deleted === 'true'" slot="subTitle">
      <v-chip x-small disabled>
        {{ $t('analytics.deletedUser') }}
      </v-chip>
    </template>
  </exo-user-avatar>
  <v-tooltip v-else bottom>
    <template v-slot:activator="{ on, attrs }">
      <i
        class="uiIconColorInfo"
        v-bind="attrs"
        v-on="on"></i>
    </template>
    <span>{{ $t('analytics.errorRetrievingIdentityWithId', {0: value}) }}</span>
  </v-tooltip>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: function() {
        return null;
      },
    },
    item: {
      type: Object,
      default: function() {
        return null;
      },
    },
    labels: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    loading: true,
    error: false,
    user: null,
  }),
  created() {
    if (this.value && this.value.username) {
      this.user = this.value;
      this.loading = false;
    } else if (this.value && Number(this.value)) {
      this.loading = true;
      this.error = false;
      this.$identityService.getIdentityById(this.value)
        .then(identity => {
          this.item.identity = this.user = identity && identity.profile;
          this.$forceUpdate();
        })
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    } else {
      this.loading = false;
    }
  },
};
</script>