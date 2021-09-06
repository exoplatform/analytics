<template>
  <v-expansion-panel class="border-box-sizing">
    <v-expansion-panel-header>
      <v-row no-gutters>
        <v-col cols="8">
          <v-fade-transition leave-absolute>
            <v-row no-gutters class="sampleItemHeader">
              <v-col
                v-if="userIdentity || userModifierIdentity"
                class="text-truncate">
                <analytics-profile-chip :identity="userIdentity || userModifierIdentity" />
              </v-col>
              <v-col
                v-if="chartData.operation"
                :title="chartData.operation"
                class="text-truncate">
                <template v-if="userIdentity || userModifierIdentity">
                  - 
                </template>
                <strong>{{ chartData.operation }}</strong>
              </v-col>
            </v-row>
          </v-fade-transition>
        </v-col>
        <v-col cols="4" class="text--secondary text-right">
          <date-format :value="chartDataTime" :format="dateFormat" />
        </v-col>
      </v-row>
    </v-expansion-panel-header>
    <v-expansion-panel-content>
      <template v-if="chartDataProps">
        <v-row
          v-for="chartDataProp in chartDataProps"
          :key="chartDataProp"
          class="ma-2"
          no-gutters>
          <v-col>{{ getI18N(chartDataProp) }}</v-col>
          <v-col v-if="chartDataProp === 'userId' && userIdentity" class="text--secondary">
            {{ chartData[chartDataProp] }}
            (<analytics-profile-chip :identity="userIdentity" />)
          </v-col>
          <v-col v-else-if="chartDataProp === 'spaceId' && spaceIdentity" class="text--secondary">
            {{ chartData[chartDataProp] }}
            (<analytics-profile-chip :identity="spaceIdentity" />)
          </v-col>
          <v-col v-else class="text--secondary">
            {{ chartData[chartDataProp] }}
          </v-col>
        </v-row>
      </template>
      <template v-if="chartDataParameters">
        <v-row
          v-for="chartDataParameter in chartDataParameters"
          :key="chartDataParameter"
          class="ma-2"
          no-gutters>
          <v-col>{{ getI18N(chartDataParameter) }}</v-col>
          <v-col v-if="chartDataParameter === 'modifierSocialId' && userModifierIdentity" class="text--secondary">
            {{ chartData.parameters[chartDataParameter] }}
            (<analytics-profile-chip :identity="userModifierIdentity" />)
          </v-col>
          <v-col v-if="chartDataParameter === 'contentId'" class="text--secondary block">
            {{ chartData.parameters[chartDataParameter] }}
            (<analytics-content-chip :content-id="chartData.parameters[chartDataParameter]" />)
          </v-col>
          <v-col v-else class="text--secondary">
            {{ chartData.parameters[chartDataParameter] }}
          </v-col>
        </v-row>
      </template>
    </v-expansion-panel-content>
  </v-expansion-panel>
</template>

<script>
export default {
  props: {
    chartData: {
      type: Object,
      default: function() {
        return null;
      },
    },
    users: {
      type: Object,
      default: function() {
        return null;
      },
    },
    spaces: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      second: 'numeric',
    },
  }),
  computed: {
    userIdentity() {
      if (this.chartData && this.chartData.userId && this.users) {
        const userObj = this.users[this.chartData.userId];
        if (userObj) {
          return userObj;
        } else {
          return {
            identityId: this.chartData.userId,
          };
        }
      } else {
        return null;
      }
    },
    userModifierIdentity() {
      if (this.chartData && this.chartData.parameters && this.chartData.parameters.modifierSocialId && this.users) {
        const userObj = this.users[this.chartData.parameters.modifierSocialId];
        if (userObj) {
          return userObj;
        } else {
          return {
            identityId: this.chartData.parameters.modifierSocialId,
          };
        }
      } else {
        return null;
      }
    },
    spaceIdentity() {
      if (this.chartData && this.chartData.spaceId && this.spaces) {
        const spaceObj = this.spaces[this.chartData.spaceId];
        if (spaceObj) {
          return spaceObj;
        } else {
          return {
            spaceId: this.chartData.spaceId,
          };
        }
      } else {
        return null;
      }
    },
    chartDataTime() {
      return this.chartData && this.chartData.timestamp && new Date(this.chartData.timestamp);
    },
    chartDataProps() {
      const chartDataProps = this.chartData && Object.keys(this.chartData).sort();
      return chartDataProps.filter(item => item !== 'parameters' && this.chartData[item]);
    },
    chartDataParameters() {
      return this.chartData && this.chartData.parameters && Object.keys(this.chartData.parameters).sort();
    },
  },
  methods: {
    getI18N(label){
      const fieldLabelI18NKey = `analytics.field.label.${label}`;
      const fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
      return  fieldLabelI18NValue === fieldLabelI18NKey ? label : fieldLabelI18NValue;
    }
  },
};
</script>
