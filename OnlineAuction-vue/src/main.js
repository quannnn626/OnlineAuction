import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import axios from "axios";

// 配置 axios 基础 URL
axios.defaults.baseURL =
  process.env.NODE_ENV === "production" ? "/api" : "/api";

// 请求拦截器
axios.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// 响应拦截器
axios.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 如果返回的状态码不是 200，则视为错误
    if (res.code !== 200) {
      Vue.prototype.$message.error(res.message || "请求失败");
      return Promise.reject(new Error(res.message || "请求失败"));
    }
    return res;
  },
  (error) => {
    Vue.prototype.$message.error(error.message || "网络错误");
    return Promise.reject(error);
  },
);

Vue.config.productionTip = false;

Vue.use(ElementUI);

// 将 axios 挂载到 Vue 原型上
Vue.prototype.$http = axios;

new Vue({
  router,
  render: (h) => h(App),
}).$mount("#app");
