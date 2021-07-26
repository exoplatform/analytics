<template>
  <v-progress-circular
    v-if="loading"
    size="24"
    color="primary"
    indeterminate />
  <div v-else-if="content"><a :href="contentUrl">{{ contentTitle }}</a></div>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: function () {
        return null;
      },
    }
  },
  data: () => ({
    loading: true,
    error: false,
    content: null,
  }),
  computed: {
    contentTitle() {
      return this.content && this.content.title;
    },
    contentUrl() {
      return this.content && this.content.url;
    },
  },
  created() {
    if (this.value) {
      this.loading = true;
      this.error = false;
      this.$analyticsUtils.getContent(this.value).then(content => {
        this.content = content;
        this.$forceUpdate();
      })
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    }
  },
};
</script>