<template>
  <div class="app-container">
    <el-container>
      <!-- 左侧菜单 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h2>拍卖系统</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          @select="handleMenuSelect"
        >
          <template v-for="menu in menuTree">
            <el-menu-item
              v-if="!menu.children || menu.children.length === 0"
              :key="menu.id"
              :index="getMenuPath(menu)"
            >
              <i :class="menu.menuIcon || 'el-icon-menu'"></i>
              <span slot="title">{{ menu.menuName }}</span>
            </el-menu-item>
            <el-submenu v-else :key="menu.id" :index="String(menu.id)">
              <template slot="title">
                <i :class="menu.menuIcon || 'el-icon-menu'"></i>
                <span>{{ menu.menuName }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.id"
                :index="getMenuPath(child)"
              >
                {{ child.menuName }}
              </el-menu-item>
            </el-submenu>
          </template>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-content">
            <h3>{{ currentPageTitle }}</h3>
            <div class="header-right">
              <el-dropdown>
                <span class="user-info">
                  <i class="el-icon-user"></i>
                  <span>用户</span>
                  <i class="el-icon-arrow-down"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>个人中心</el-dropdown-item>
                  <el-dropdown-item divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { getMenuTree } from "@/api/menu";

export default {
  name: "Index",
  data() {
    return {
      menuTree: [],
      activeMenu: "",
      currentPageTitle: "首页",
      menuMap: {},
      loading: false,
    };
  },
  mounted() {
    this.loadMenus();
    this.setActiveMenu();
    this.updatePageTitle();
  },
  watch: {
    $route(to) {
      this.setActiveMenu();
      this.updatePageTitle();
    },
  },
  methods: {
    async loadMenus() {
      this.loading = true;
      try {
        console.log("开始加载菜单...");
        const data = await getMenuTree(true);
        console.log("菜单数据:", data);
        this.menuTree = data || [];
        this.buildMenuMap(this.menuTree);
        console.log("菜单加载成功，菜单数量:", this.menuTree.length);
      } catch (error) {
        console.error("加载菜单失败:", error);
        console.error("错误详情:", error.response || error.message);
        // 即使加载失败，也显示空菜单，让页面可以正常显示
        this.menuTree = [];
        this.$message.warning("菜单加载失败，请检查后端服务是否正常运行");
      } finally {
        this.loading = false;
      }
    },
    buildMenuMap(menus) {
      this.menuMap = {}; // 重置菜单映射
      menus.forEach((menu) => {
        if (menu.menuPath) {
          const path = this.getMenuPath(menu);
          this.menuMap[path] = menu;
          console.log("菜单映射:", path, "->", menu.menuName);
        }
        if (menu.children && menu.children.length > 0) {
          this.buildMenuMap(menu.children);
        }
      });
      console.log("菜单映射表:", Object.keys(this.menuMap));
      // 构建完成后，更新当前激活菜单
      this.setActiveMenu();
    },
    getMenuPath(menu) {
      if (!menu.menuPath) return "";
      if (menu.menuPath.startsWith("http")) {
        return menu.menuPath;
      }
      // 保持路径格式，确保以 / 开头（Element UI menu 的 index 需要完整路径）
      return menu.menuPath.startsWith("/")
        ? menu.menuPath
        : "/" + menu.menuPath;
    },
    setActiveMenu() {
      // 设置当前激活的菜单项
      const currentPath = this.$route.path;
      console.log("设置激活菜单，当前路径:", currentPath);
      // 先尝试精确匹配
      if (this.menuMap[currentPath]) {
        this.activeMenu = currentPath;
        console.log("精确匹配成功:", currentPath);
        return;
      }
      // 如果精确匹配失败，尝试匹配父路径（用于嵌套路由）
      for (const path in this.menuMap) {
        if (currentPath.startsWith(path) && path !== "/") {
          this.activeMenu = path;
          console.log("父路径匹配成功:", path);
          return;
        }
      }
      // 如果都匹配不上，使用当前路由路径
      this.activeMenu = currentPath;
      console.log("使用当前路径作为激活菜单:", currentPath);
    },
    handleMenuSelect(path) {
      this.activeMenu = path;
      const menu = this.menuMap[path];
      if (menu) {
        this.currentPageTitle = menu.menuName;
      }
    },
    updatePageTitle() {
      const currentPath = this.$route.path;
      // 先尝试精确匹配
      let menu = this.menuMap[currentPath];
      // 如果精确匹配失败，尝试匹配父路径
      if (!menu) {
        for (const path in this.menuMap) {
          if (currentPath.startsWith(path) && path !== "/") {
            menu = this.menuMap[path];
            break;
          }
        }
      }
      if (menu) {
        this.currentPageTitle = menu.menuName;
      } else {
        // 根据路由设置标题
        const routeName = this.$route.name;
        const titleMap = {
          Home: "首页",
          Goods: "拍卖商品",
          Notice: "竞拍公告",
          Profile: "个人中心",
        };
        this.currentPageTitle = titleMap[routeName] || "在线拍卖系统";
      }
    },
  },
};
</script>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
}

.app-container ::v-deep .el-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1000;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  background-color: #2b3a4a;
  position: sticky;
  top: 0;
  z-index: 10;
}

.logo h2 {
  font-size: 18px;
  font-weight: 500;
  margin: 0;
}

.sidebar-menu {
  border: none;
  background-color: #304156;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-submenu__title {
  color: #bfcbd9;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-submenu__title:hover {
  background-color: #263445;
  color: #409eff;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #409eff;
  color: #fff;
}

.app-container ::v-deep .el-container > .el-container {
  margin-left: 200px;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0;
  flex-shrink: 0;
  height: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.header-content h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 5px;
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  height: 0;
  min-height: 0;
}
</style>
