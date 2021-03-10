<template>
  <v-menu
    v-model="menu"
    :content-class="menuId"
    class="analytics-selected-period-menu"
    :close-on-content-click="false"
    :nudge-right="selectedTime ? -306 : -100"
    transition="scale-transition"
    offset-y
    attach
    min-width="auto">
    <template v-slot:activator="{ on, attrs }">
      <v-text-field
        v-model="dateRangeText"
        prepend-inner-icon="mdi-calendar"
        class="pt-0 mt-0"
        rel="tooltip"
        :title="$t('analytics.period', {0: from, 1: to})"
        readonly
        v-bind="attrs"
        v-on="on" />
    </template>
    <div class="d-flex date-time-picker">
      <v-date-picker
        v-model="dates"
        :locale="lang"
        class="analyticsDatePicker"
        range
        scrollable
        @input="selectedCustomDate()">
        <div class="dateFooter footer">
          <div class="caption font-italic font-weight-light pl-1 muted">
            {{ $t('analytics.period', {0: from, 1: to}) }}
          </div>
          <v-btn-toggle
            v-model="defaultSelected"
            class="d-flex flex-wrap justify-space-between defaultPeriodAnalytics"
            tile
            color="primary"
            background-color="primary"
            group>
            <v-btn
              v-for="(item, index) in periodOptions"
              :key="index"
              class="my-0"
              small
              :value="item.value"
              @click="selectItem(item)">
              <div>{{ item.text }}</div>
            </v-btn>
          </v-btn-toggle>
        </div>
      </v-date-picker>
      <v-time-picker
        v-show="selectedTime"
        v-model="time"
        :format="lang==='fr' ? '24hr' : 'ampm'"
        class="time-picker-analytics"
        ampm-in-title>
        <v-btn
          class="btn"
          text
          @click="saveTime">
          Select Time
        </v-btn>
      </v-time-picker>
    </div>
  </v-menu>
</template>

<script>
export default {
  props: ['value'],
  data: () => ({
    dates: [],
    menu: false,
    lang: eXo.env.portal.language,
    selectedTime:false,
    time:null,
    defaultValue: 'last6Months',
    selectedItem: null,
    defaultSelected: 'last6Months',
    menuId: `AnalyticsDatePickerMenu${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    times: []
  }),
  computed:{
    periodOptions() {
      return [
        {
          value: 'lastYear',
          text: this.$t('analytics.periodOptions.lastYear')
        },
        {
          value: 'last6Months',
          text: this.$t('analytics.periodOptions.last6Months'),
        },
        {
          value: 'last3Months',
          text: this.$t('analytics.periodOptions.last3Months')
        },
        {
          value: 'lastMonth',
          text: this.$t('analytics.periodOptions.lastMonth')
        },
        {
          value: 'lastWeek',
          text: this.$t('analytics.periodOptions.lastWeek')
        },
        {
          value: 'last24h',
          text: this.$t('analytics.periodOptions.last24h')
        },
      ];
    },
    period(){
      return this.value;
    },
    dateRangeText () {
      return this.dates.join('~');
    },
    from(){
      return this.value && new Date(this.value.min).toLocaleString(this.lang,{year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit'});
    },
    to(){
      return this.value && new Date(this.value.max).toLocaleString(this.lang,{year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit'});
    }
  },
  watch: {
    period(){
      this.definePeriod();
    },
    menu(){
      if((this.dates.length < 2 || this.times.length < 2 ) && !this.menu) {
        this.definePeriod();
      }
    },
  },
  mounted() {
    $(document).on('click', (e) => {
      if (e.target && !$(e.target).parents(`.${this.menuId}`).length) {
        this.menu = false;
        this.selectedTime=false;
      }
    });
    const defaultItem = this.periodOptions.find(item => item.value === this.defaultValue);
    this.selectItem(defaultItem);
  },
  methods: {
    saveTime(){
      this.times.push(this.time);
      //check if period selected
      if (this.times && this.times.length === 2 && this.dates.length === 2) {
        // permutation if end date > start date
        if (new Date(this.dates[0]) > new Date(this.dates[1])){
          const value = this.dates[0];
          const valueTime = this.times[0];
          this.times[0] = this.times[1];
          this.times[1] = valueTime;
          this.dates[0] = this.dates[1];
          this.dates[1] = value;
        }
        const selectedPeriod = {
          min: new Date(`${this.dates[0] } ${ this.times[0]}`).getTime(),
          max: new Date(`${this.dates[1] } ${  this.times[1]}`).getTime()
        };
        this.$emit('input', selectedPeriod);
        this.$forceUpdate();
        this.value = selectedPeriod;
        //initialize variables
        this.defaultSelected = null;
        this.selectedTime = false;
        this.menu = false;
        this.times = [];
        this.time = null;
      } else {
        this.selectedTime = false;
        this.menu = true;
      }

    },
    definePeriod(){
      if (this.value){
        this.dates = [];
        this.dates.push(new Date(this.period.min).toISOString().slice(0,10));
        this.dates.push(new Date(this.period.max).toISOString().slice(0,10));
        this.times = [];
        this.time = null;
      }
    },
    selectedCustomDate(){
      this.selectedTime = true;
      if (!this.time) {
        const currentDate= new Date();
        this.time = `${currentDate.getHours()}:${currentDate.getMinutes()}`;
      }
    },
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
      selectedPeriod.period = this.selectedItem.value;
      this.$emit('input', selectedPeriod);
      this.value = selectedPeriod;
      this.$forceUpdate();
      this.menu = false;
      this.selectedTime = false;
    },
  },
};
</script>
