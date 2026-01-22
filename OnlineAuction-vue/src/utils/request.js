import axios from "axios";
import { Message } from "element-ui";

// 创建 axios 实例
const service = axios.create({
  // 设置 axios 实例的基础 URL，所有相对请求会被拼接到该前缀，请求超时为10秒
  baseURL: process.env.NODE_ENV === "production" ? "/api" : "/api",
  timeout: 10000,
  withCredentials: true, // 允许携带Cookie（用于Session认证）
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // withCredentials已在axios实例中配置，会自动携带Cookie
    return config;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    console.log("API响应:", res); // 调试日志
    // 如果返回的状态码不是 200，则视为错误
    if (res.code !== 200) {
      const errorMessage = res.message || "请求失败";
      Message.error(errorMessage);
      const error = new Error(errorMessage);
      error.response = response; // 保存完整的响应对象，方便前端获取详细信息
      return Promise.reject(error);
    }
    // 返回数据部分
    return res.data;
  },
  (error) => {
    console.error("响应错误:", error);
    // 更详细的错误信息
    let errorMessage = "网络错误";
    if (error.response) {
      // 服务器返回了错误状态码
      errorMessage = `请求失败: ${error.response.status} ${error.response.statusText}`;
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage = "无法连接到服务器，请检查后端服务是否启动";
    } else {
      // 其他错误
      errorMessage = error.message || "网络错误";
    }
    Message.error(errorMessage);
    return Promise.reject(error);
  }
);
//导出配置好拦截器的 axios 实例，项目中所有 API 请求通过这个实例发出，会自动走上面逻辑
export default service;
