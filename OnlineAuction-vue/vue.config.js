module.exports = {
  devServer: {
    port: 3000,
    proxy: {
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
