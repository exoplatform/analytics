<template>
  <v-app id="breadcrumb">
    <h1>breadcrumb</h1>
  </v-app>
</template>
<script>
export default {
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  }
};
</script>
