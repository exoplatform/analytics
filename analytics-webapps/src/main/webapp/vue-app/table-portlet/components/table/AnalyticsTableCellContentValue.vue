<template>
  <v-progress-circular
    v-if="loading"
    size="24"
    color="primary"
    indeterminate />
  <div v-else-if="content">{{ this.content.title }}</div>
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
  created() {
    if (this.value) {
      this.loading = true;
      this.error = false;
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${this.value}`, {
        credentials: 'include',
        method: 'GET',
      }).then((resp) => {
        if (resp && resp.ok) {
          return resp.json();
        }
        return null;
      }).then(content => {
        this.content = content;
        this.$forceUpdate();
      })
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    }
  },
};
</script>