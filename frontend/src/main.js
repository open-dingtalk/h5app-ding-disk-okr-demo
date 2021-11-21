import Vue from 'vue'
import App from './App.vue'
import * as dd from 'dingtalk-jsapi' 
window.dd = dd

Vue.config.productionTip = false

new Vue({
  render: h => h(App)
}).$mount('#app')
