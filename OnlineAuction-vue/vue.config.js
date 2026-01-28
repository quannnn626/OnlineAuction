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
        onProxyReq: (proxyReq, req, res) => {
          console.log("代理静态资源请求:", req.method, req.url, "->", proxyReq.path);
        },
        onProxyRes: (proxyRes, req, res) => {
          console.log("静态资源响应状态:", proxyRes.statusCode, req.url);
        },
        onError: (err, req, res) => {
          console.error("静态资源代理错误:", err.message, req.url);
        },
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
        onProxyReq: (proxyReq, req, res) => {
          // 确保 Cookie 被正确转发
          if (req.headers.cookie) {
            proxyReq.setHeader("Cookie", req.headers.cookie);
          }
          console.log("代理请求:", req.method, req.url);
          console.log("请求Cookie:", req.headers.cookie);
        },
        onProxyRes: (proxyRes, req, res) => {
          // 确保 Set-Cookie 头被正确传递
          if (proxyRes.headers["set-cookie"]) {
            console.log("响应Set-Cookie:", proxyRes.headers["set-cookie"]);
          }
        },
      },
    },
  },
  publicPath: process.env.NODE_ENV === "production" ? "./" : "/",
  outputDir: "dist",
  assetsDir: "static",
};
