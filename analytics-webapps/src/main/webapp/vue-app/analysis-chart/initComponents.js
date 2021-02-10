import AnalysisChart from './components/AnalysisChart.vue';

const components = {
  'analysis-chart': AnalysisChart,
};

for (const key in components) {
  Vue.component(key, components[key]);
}