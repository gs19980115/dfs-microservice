// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
// 引入 Element UI 组件 （参考:http://element.eleme.io/#/zh-CN/component/installation）
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
Vue.use(ElementUI);

//引入 axios （参考：https://www.kancloud.cn/yunye/axios/234845）
import axios from 'axios'

// 引入 v-charts (参考 ：https://v-charts.js.org/#/start)
import VCharts from 'v-charts'
Vue.use(VCharts)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  render: h => h(App),
  template: '<App/>'
})