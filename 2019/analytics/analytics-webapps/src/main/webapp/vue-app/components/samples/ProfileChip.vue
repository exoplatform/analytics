<template>
  <a
    v-if="displayName"
    :title="displayName"
    :href="url"
    class="text-truncate"
    rel="nofollow"
    target="_blank">
    {{ displayName }}
  </a>
  <code v-else>
    {{ spaceId || identityId }}
  </code>
</template>

<script>

export default {
  props: {
    identity: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  computed: {
    identityId() {
      return this.identity && this.identity.identityId;
    },
    remoteId() {
      return this.identity && this.identity.remoteId;
    },
    providerId() {
      return this.identity && this.identity.providerId;
    },
    avatar() {
      return this.identity && this.identity.avatar;
    },
    spaceId() {
      return this.identity && this.identity.spaceId;
    },
    displayName() {
      return this.identity && this.identity.displayName;
    },
    url() {
      if (this.providerId === 'organization') {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.remoteId}`;
      } else if (this.providerId === 'space') {
        return `${eXo.env.portal.context}/g/:spaces:${this.remoteId}/`;
      }
      return '';
    },
  },
};
</script>
