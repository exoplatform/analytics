const path = require('path');
const merge = require('webpack-merge');

const webpackProductionConfig = require('./webpack.prod.js');

module.exports = merge(webpackProductionConfig, {
  output: {
    path: '/home/exo/eXo-work/FT_Analytics/Server_Project/platform-6.2.x-analytics-SNAPSHOT/webapps/analytics/',
    filename: 'js/[name].bundle.js'
  }
});
