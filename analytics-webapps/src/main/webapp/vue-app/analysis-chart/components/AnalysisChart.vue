<template>
  <v-app>
    <v-card class="analytics-chart-parent white" flat>
      <div class="d-flex pa-3 analytics-chart-header" flat>
        <v-spacer />
        <analytics-select-period v-model="selectedPeriod" />
        <v-menu
          v-model="showMenu"
          offset-y>
          <template v-slot:activator="{ on }">
            <v-btn
              icon
              class="ml-2"
              v-on="on"
              @blur="closeMenu()">
              <v-icon>mdi-dots-vertical</v-icon>
            </v-btn>
          </template>
          <v-list>
            <v-list-item @mousedown="$event.preventDefault()">
              <v-list-item-title>{{ $t('analytics.samples') }}</v-list-item-title>
            </v-list-item>
            <v-list-item @mousedown="$event.preventDefault()">
              <v-list-item-title>{{ $t('analytics.settings') }}</v-list-item-title>
            </v-list-item>
            <v-list-item @mousedown="$event.preventDefault()">
              <v-list-item-title>{{ $t('analytics.jsonSettings') }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </v-card>
  </v-app>
</template>
<script>
export default {
  data : () =>({
    selectedPeriod: null,
    showMenu:false,
  }),
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    closeMenu(){
      this.showMenu=false;
    }
  }
};
</script>