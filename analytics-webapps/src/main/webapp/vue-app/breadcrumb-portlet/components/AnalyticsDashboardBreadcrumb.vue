<template>
  <v-app>
    <v-card class="border-radius" flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              <strong>{{ $t('analytics.application') }}</strong>
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-card>
  </v-app>
</template>
<script>
export default {
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  }
};
</script>