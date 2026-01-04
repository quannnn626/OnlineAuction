# 前后端分离改造说明

## 改造内容

### 后端改造

1. **配置 CORS 跨域支持**
   - 修改 `WebMvcConfig.java`，添加 CORS 配置
   - 允许前端访问 `/api/**` 路径

2. **创建统一响应类**
   - 新增 `Result.java` 统一响应格式
   - 所有 API 返回格式：`{ code: 200, message: "成功", data: {} }`

3. **更新 API 控制器**
   - `MenuApiController` 已更新为使用 `Result` 类
   - 其他 Controller 可以按相同方式改造

4. **移除页面路由**
   - 删除 `PageController.java`（不再返回 HTML 模板）
   - 移除 `application.yml` 中的 FreeMarker 配置

### 前端项目

1. **项目结构**
   ```
   frontend/
   ├── public/          # 静态资源
   ├── src/
   │   ├── api/        # API 接口
   │   ├── assets/     # 资源文件
   │   ├── components/ # 公共组件
   │   ├── router/     # 路由配置
   │   ├── utils/      # 工具函数
   │   ├── views/      # 页面组件
   │   ├── App.vue     # 根组件
   │   └── main.js     # 入口文件
   ├── package.json
   └── vue.config.js
   ```

2. **技术栈**
   - Vue 2.6
   - Vue Router 3.5
   - Element UI 2.15
   - Axios 0.27

3. **已创建的页面**
   - 首页（Home.vue）- 已迁移完成
   - 商品详情（GoodsDetail.vue）- 占位页面
   - 其他页面 - 占位页面，待完善

## 使用说明

### 后端启动

```bash
# 确保后端服务运行在 8080 端口
mvn spring-boot:run
```

### 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run serve
```

前端服务将运行在 http://localhost:3000

### 开发流程

1. **后端开发**
   - 在 `controller` 包下创建 RESTful API
   - 使用 `Result` 类统一返回格式
   - API 路径统一使用 `/api/**` 前缀

2. **前端开发**
   - 在 `src/api/` 目录下创建 API 调用文件
   - 在 `src/views/` 目录下创建页面组件
   - 使用 `this.$http` 或 `request` 工具调用后端 API

### API 调用示例

```javascript
// src/api/menu.js
import request from '@/utils/request'

export function getMenuTree(all = false) {
  return request({
    url: '/menu/tree',
    method: 'get',
    params: { all }
  })
}

// 在组件中使用
import { getMenuTree } from '@/api/menu'

async loadMenus() {
  const data = await getMenuTree(true)
  this.menuTree = data
}
```

## 后续工作

1. **完善页面组件**
   - 将 `templates/` 目录下的 HTML 页面迁移到 Vue 组件
   - 实现商品详情、订单管理等完整功能

2. **完善 API 接口**
   - 为各个模块创建完整的 CRUD API
   - 添加用户认证和权限控制

3. **优化前端**
   - 添加状态管理（Vuex）
   - 优化路由配置
   - 添加错误处理和加载状态

4. **部署配置**
   - 配置生产环境构建
   - 配置 Nginx 反向代理

## 注意事项

1. **跨域问题**
   - 开发环境：前端通过 `vue.config.js` 中的 proxy 代理到后端
   - 生产环境：需要配置 Nginx 或后端 CORS 策略

2. **API 路径**
   - 所有 API 统一使用 `/api/` 前缀
   - 前端请求会自动添加 `/api` 前缀

3. **响应格式**
   - 后端统一返回 `Result` 格式
   - 前端拦截器会自动提取 `data` 字段

