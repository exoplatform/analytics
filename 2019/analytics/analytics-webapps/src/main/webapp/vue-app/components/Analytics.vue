<template>
  <v-app 
    id="AnalyticsApp"
    class="transparent"
    flat>
    <main>
      <v-layout justify-center>
        <v-flex xs12>
          <collabative-publication-analytics />
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
import CollabativePublicationAnalytics from './CollabativePublicationAnalytics.vue';

export default {
  components: {
    CollabativePublicationAnalytics,
  },
};
</script>