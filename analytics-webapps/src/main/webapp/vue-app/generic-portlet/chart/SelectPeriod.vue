<template>
  <v-menu
    close-on-click
    open-on-hover>
    <template v-slot:activator="{ on }">
      <v-btn
        class="btn"
        v-on="on">
        {{ selectedItem && selectedItem.text }}
      </v-btn>
    </template>
    <v-list>
      <v-list-item
        v-for="(item, index) in periodOptions"
        :key="index"
        @click="selectItem(item)">
        <v-list-item-title>{{ item.text }}</v-list-item-title>
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
          value: 'last24h',
          text: 'Last 24 hours',
        },
        {
          value: 'lastWeek',
          text: 'Last week',
        },
        {
          value: 'lastMonth',
          text: 'Last month',
        },
        {
          value: 'last3Months',
          text: 'Last 3 month',
        },
        {
          value: 'last6Months',
          text: 'Last 6 month',
        },
        {
          value: 'lastYear',
          text: 'Last year',
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