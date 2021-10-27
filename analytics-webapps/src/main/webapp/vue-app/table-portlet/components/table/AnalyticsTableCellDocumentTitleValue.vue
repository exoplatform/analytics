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
  <div
    v-else
    class="clickable primary--text text-truncate"
    :style="`max-width: ${cellWidth}`"
    @click="openPreview">
    {{ attachment.title }}
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: () => null,
    },
    column: {
      type: Object,
      default: () => null,
    },
  },
  data: () => ({
    loading: true,
    error: false,
    attachment: {},
  }),
  computed: {
    cellWidth() {
      return this.column && this.column.width;
    }
  },
  created() {
    if (this.value) {
      this.loading = true;
      this.error = false;
      this.$attachmentService.getAttachmentById(this.value)
        .then(attachment => {
          this.attachment = attachment;
        })
        .catch(() => this.error = true)
        .finally(() => this.loading = false);
    } else {
      this.loading = false;
    }
  },
  methods: {
    openPreview() {
      documentPreview.init({
        doc: {
          id: this.attachment.id,
          repository: 'repository',
          workspace: 'collaboration',
          path: this.attachment.path,
          title: this.attachment.title,
          icon: this.attachment.icon,
          size: this.attachment.size,
          openUrl: this.attachment.openUrl,
          downloadUrl: this.attachment.downloadUrl,
        },
        author: this.attachment.updater,
        showComments: false,
      });
    },
  },
};
</script>