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
      },
    },
  },
  publicPath: process.env.NODE_ENV === "production" ? "./" : "/",
  outputDir: "dist",
  assetsDir: "static",
};
