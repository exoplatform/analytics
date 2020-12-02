<template>
  <v-expansion-panel class="border-box-sizing">
    <v-expansion-panel-header>
      <v-row no-gutters>
        <v-col cols="8">
          <v-fade-transition leave-absolute>
            <v-row no-gutters class="sampleItemHeader">
              <v-col
                v-if="userIdentity"
                class="text-truncate">
                User: <profile-chip :identity="userIdentity" /> 
              </v-col>
              <v-col
                v-else-if="userModifierIdentity"
                class="text-truncate">
                Modifier user: <profile-chip :identity="userModifierIdentity" />
              </v-col>
              <v-col
                v-if="chartData.operation"
                class="text-truncate">
                Operation:  <strong>{{ chartData.operation }}</strong>
              </v-col>
            </v-row>
          </v-fade-transition>
        </v-col>
        <v-col cols="4" class="text--secondary text-right">{{ chartDataTime }}</v-col>
      </v-row>
    </v-expansion-panel-header>
    <v-expansion-panel-content>
      <template v-if="chartDataProps">
        <v-row
          v-for="chartDataProp in chartDataProps"
          :key="chartDataProp"
          class="ma-2"
          no-gutters>
          <v-col>{{ chartDataProp }}</v-col>
          <v-col v-if="chartDataProp === 'userId' && userIdentity" class="text--secondary">
            {{ chartData[chartDataProp] }}
            (<profile-chip :identity="userIdentity" />)
          </v-col>
          <v-col v-else-if="chartDataProp === 'spaceId' && spaceIdentity" class="text--secondary">
            {{ chartData[chartDataProp] }}
            (<profile-chip :identity="spaceIdentity" />)
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
          <v-col>{{ chartDataParameter }}</v-col>
          <v-col v-if="chartDataParameter === 'modifierSocialId' && userModifierIdentity" class="text--secondary">
            {{ chartData.parameters[chartDataParameter] }}
            (<profile-chip :identity="userModifierIdentity" />)
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
import ProfileChip from './ProfileChip.vue';

export default {
  components: {
    ProfileChip,
  },
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
      return (this.chartData && this.chartData.timestamp && this.formatDate(this.chartData.timestamp)) || '';
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
    formatDate(timeInMilliseconds) {
      const dateTime = new Date(timeInMilliseconds);
      const lang = eXo.env.portal.language;
      return dateTime.toLocaleString(lang);
    },
  },
};
</script>
