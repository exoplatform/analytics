<template>
  <v-menu
    close-on-click
    open-on-hover>
    <template v-slot:activator="{ on }">
      <v-btn
        class="btn"
        v-on="on">
        {{ selectedItem && $t(`analytics.periodOptions.`.concat(selectedItem.value)) }}
      </v-btn>
    </template>
    <v-list>
      <v-list-item
        v-for="(item, index) in periodOptions"
        :key="index"
        @click="selectItem(item)">
        <v-list-item-title>{{ $t(`analytics.periodOptions.`.concat(item.value)) }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script>
export default {
  data () {
    return {
      defaultValue: 'last6Months',
      selectedItem: null,
      periodOptions: [
        {
          value: 'last24h'
        },
        {
          value: 'lastWeek'
        },
        {
          value: 'lastMonth'
        },
        {
          value: 'last3Months'
        },
        {
          value: 'last6Months'
        },
        {
          value: 'lastYear'
        },
      ],
    };
  },
  mounted() {
    const defaultItem = this.periodOptions.find(item => item.value === this.defaultValue);
    this.selectItem(defaultItem);
  },
  methods: {
    selectItem(item){
      this.selectedItem = item;

      const nowInMS = Date.now();
      const selectedPeriod = {
        max: nowInMS,
      };
      switch (this.selectedItem.value) {
      case 'last24h':
        selectedPeriod.min = nowInMS - 86400000;
        break;
      case 'lastWeek':
        selectedPeriod.min = nowInMS - 604800000;
        break;
      case 'lastMonth':
        selectedPeriod.min = nowInMS - 2592000000;
        break;
      case 'last3Months':
        selectedPeriod.min = nowInMS - 7776000000;
        break;
      case 'last6Months':
        selectedPeriod.min = nowInMS - 15552000000;
        break;
      case 'lastYear':
        selectedPeriod.min = nowInMS - 31536000000;
        break;
      }
      this.$emit('input', selectedPeriod);
      this.$forceUpdate();
    },
  },
}
</script>