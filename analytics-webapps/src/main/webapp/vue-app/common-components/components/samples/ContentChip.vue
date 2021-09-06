<template>
  <a
    v-if="contentTitle"
    :title="contentTitle"
    :href="contentUrl"
    class="text-truncate"
    rel="nofollow"
    target="_blank">
    {{ contentTitle }}
  </a>
  <code v-else>
    {{ contentId }}
  </code>
</template>

<script>

export default {
  props: {
    contentId: {
      type: String,
      default: ''
    },
  },
  data: () => ({
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
    this.$analyticsUtils.getContent(this.contentId).then(content => {
      this.content = content;
    });
  }
};
</script>
