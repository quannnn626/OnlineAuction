module.exports = {
  devServer: {
    port: 3000,
    proxy: {
      // 静态资源代理（上传的文件）- 必须在/api之前配置，确保优先匹配
      "/upload": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
        logLevel: "debug",
        onProxyReq: () => {},
        onProxyRes: () => {},
        onError: () => {},
      },
      // 统一API代理（所有/api开头的请求都转发到后端）
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        ws: true,
        secure: false,
        logLevel: "debug",
        // 确保 Cookie 正确传递
        cookieDomainRewrite: "",
        onProxyReq: (proxyReq, req) => {
          if (req.headers.cookie) {
            proxyReq.setHeader("Cookie", req.headers.cookie);
          }
        },
        onProxyRes: () => {},
      },
    },
  },
  publicPath: process.env.NODE_ENV === "production" ? "./" : "/",
  outputDir: "dist",
  assetsDir: "static",
};
