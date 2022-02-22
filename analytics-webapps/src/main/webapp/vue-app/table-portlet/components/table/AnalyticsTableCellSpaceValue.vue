<template>
  <v-progress-circular
    v-if="loading"
    size="24"
    color="primary"
    indeterminate />
  <div v-else-if="error" class="d-flex">
    <i :title="$t('analytics.errorRetrievingDataForValue', {0: value})" class="uiIconColorError my-auto"></i>
    <span class="text-no-wrap text-sub-title my-auto ml-1">
      {{ $t('analytics.deletedSpace') }}
    </span>
  </div>
  <exo-space-avatar
    v-else-if="space"
    :space="space"
    :size="32"
    avatar-class="border-color"
    extra-class="analytics-table-space d-inline-block my-1"
    popover />
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
    space: null,
  }),
  created() {
    if (this.value && this.value.prettyName) {
      this.space = this.value;
      this.loading = false;
    } else if (this.value) {
      this.loading = true;
      this.error = false;
      this.$spaceService.getSpaceById(this.value)
        .then(space => {
          this.item.identity = this.space = space;
          this.$forceUpdate();
        })
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    }
  },
};
</script>