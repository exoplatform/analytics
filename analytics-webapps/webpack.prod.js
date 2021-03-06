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
    commonAnalyticsVueComponents: './src/main/webapp/vue-app/common-components/main.js',
    analytics: './src/main/webapp/vue-app/generic-portlet/main.js',
    breadcrumb: './src/main/webapp/vue-app/breadcrumb-portlet/main.js',
    analyticsRate:'./src/main/webapp/vue-app/rate-portlet/main.js',
    analyticsTable:'./src/main/webapp/vue-app/table-portlet/main.js'
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
