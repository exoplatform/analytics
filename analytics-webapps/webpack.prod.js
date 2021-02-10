const path = require('path');

const config = {
  context: path.resolve(__dirname, '.'),
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
          'eslint-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
          'eslint-loader',
        ]
      }
    ]
  },
  entry: {
    analytics: './src/main/webapp/vue-app/generic-portlet/main.js',
    breadcrumb: './src/main/webapp/vue-app/breadcrumb-portlet/main.js',
    activeUsers: './src/main/webapp/vue-app/active-users/main.js',
    analysisChart: './src/main/webapp/vue-app/analysis-chart/main.js'
  },
  output: {
    path: path.join(__dirname, 'target/analytics/'),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  externals: {
    vue: 'Vue',
    vuetify: 'Vuetify',
    jquery: '$',
  },
};

module.exports = config;
