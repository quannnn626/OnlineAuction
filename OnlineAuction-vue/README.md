# 在线拍卖系统 - 前端项目

## 项目结构

```
frontend/
├── public/          # 静态资源
├── src/
│   ├── assets/     # 资源文件
│   ├── components/ # 公共组件
│   ├── router/     # 路由配置
│   ├── views/      # 页面组件
│   ├── utils/      # 工具函数
│   ├── App.vue     # 根组件
│   └── main.js     # 入口文件
├── package.json
└── vue.config.js
```

## 安装依赖

```bash
cd frontend
npm install
```

## 开发运行

```bash
npm run serve
```

前端服务将运行在 http://localhost:3000

## 构建生产版本

```bash
npm run build
```

构建后的文件将输出到 `dist/` 目录

## 技术栈

- Vue 2.6
- Vue Router 3.5
- Element UI 2.15
- Axios 0.27

