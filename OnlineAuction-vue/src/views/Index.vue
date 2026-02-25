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
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <i class="el-icon-user"></i>
                  <span>{{ userName || "用户" }}</span>
                  <i class="el-icon-arrow-down"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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
import { logout } from "@/api/auth";

export default {
  name: "Index",
  data() {
    return {
      menuTree: [],
      activeMenu: "",
      currentPageTitle: "首页",
      menuMap: {},
      loading: false,
      userName: "",
    };
  },
  mounted() {
    this.loadUserInfo();
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
        const data = await getMenuTree(false);
        // 前端双重保护：如果是超级管理员，过滤掉留言板菜单
        let menuTree = data || [];
        const userInfo = localStorage.getItem("userInfo");
        if (userInfo) {
          try {
            const user = JSON.parse(userInfo);
            if (user.isSuperAdmin) {
              // 过滤掉留言板菜单（ID=8 或路径为 /message-board）
              menuTree = this.filterMenu(menuTree, (menu) => {
                return menu.id !== 8 && menu.menuPath !== "/message-board";
              });
            }
          } catch (e) {
            // 忽略解析错误
          }
        }
        
        this.menuTree = menuTree;
        this.buildMenuMap(this.menuTree);
      } catch (error) {
        this.menuTree = [];
        this.$message.warning("菜单加载失败，请检查后端服务是否正常运行");
      } finally {
        this.loading = false;
      }
    },
    // 递归过滤菜单
    filterMenu(menus, filterFn) {
      if (!menus || menus.length === 0) {
        return [];
      }
      return menus
        .filter(filterFn)
        .map((menu) => {
          if (menu.children && menu.children.length > 0) {
            menu.children = this.filterMenu(menu.children, filterFn);
          }
          return menu;
        });
    },
    buildMenuMap(menus) {
      this.menuMap = {}; // 重置菜单映射
      menus.forEach((menu) => {
        if (menu.menuPath) {
          const path = this.getMenuPath(menu);
          this.menuMap[path] = menu;
        }
        if (menu.children && menu.children.length > 0) {
          this.buildMenuMap(menu.children);
        }
      });
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
      const currentPath = this.$route.path;
      if (this.menuMap[currentPath]) {
        this.activeMenu = currentPath;
        return;
      }
      for (const path in this.menuMap) {
        if (currentPath.startsWith(path) && path !== "/") {
          this.activeMenu = path;
          return;
        }
      }
      this.activeMenu = currentPath;
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
    loadUserInfo() {
      const userInfo = localStorage.getItem("userInfo");
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo);
          this.userName = user.nickName || user.userName || "用户";
        } catch (e) {
          // 忽略
        }
      }
    },
    handleCommand(command) {
      if (command === "logout") {
        this.handleLogout();
      } else if (command === "profile") {
        this.$router.push("/profile");
      }
    },
    handleLogout() {
      this.$confirm("确定要退出登录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          logout()
            .then(() => {
              // 清除本地存储
              localStorage.removeItem("userInfo");
              localStorage.removeItem("userId");
              localStorage.removeItem("userName");
              localStorage.removeItem("userRole");
              localStorage.removeItem("isAdmin");
              localStorage.removeItem("isSuperAdmin");
              localStorage.removeItem("isBuyer");
              localStorage.removeItem("isSeller");
              
              this.$message.success("退出登录成功");
              // 跳转到登录页
              this.$router.push("/login");
            })
            .catch(() => {
              localStorage.clear();
              this.$router.push("/login");
            });
        })
        .catch(() => {
          // 用户取消
        });
    },
  },
};
</script>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
}

.app-container ::v-deep .el-container {
  height: 100vh;
}

.sidebar {
  background: #001529;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: #fff;
  flex-shrink: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
}

.logo::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.logo:hover::before {
  opacity: 1;
}

.logo h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  z-index: 1;
  position: relative;
}

.sidebar-menu {
  border: none;
  background: transparent;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 0;
}

.sidebar-menu ::v-deep .el-menu-item,
.sidebar-menu ::v-deep .el-submenu__title {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  font-weight: 500;
  margin: 4px 8px;
  border-radius: 6px;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
  position: relative;
  overflow: hidden;
}

.sidebar-menu ::v-deep .el-menu-item::before,
.sidebar-menu ::v-deep .el-submenu__title::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #1890ff;
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.sidebar-menu ::v-deep .el-menu-item:hover,
.sidebar-menu ::v-deep .el-submenu__title:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #fff;
  transform: translateX(4px);
}

.sidebar-menu ::v-deep .el-menu-item:hover::before,
.sidebar-menu ::v-deep .el-submenu__title:hover::before {
  transform: scaleY(1);
}

.sidebar-menu ::v-deep .el-menu-item.is-active {
  background: #1890ff;
  color: #fff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.sidebar-menu ::v-deep .el-menu-item.is-active::before {
  transform: scaleY(1);
  background: #fff;
}

.sidebar-menu ::v-deep .el-submenu__title {
  padding-left: 24px !important;
}

.sidebar-menu ::v-deep .el-menu-item {
  padding-left: 48px !important;
}

.sidebar-menu ::v-deep .el-submenu .el-menu-item {
  padding-left: 68px !important;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
}

.sidebar-menu ::v-deep .el-submenu .el-menu-item:hover {
  background: rgba(24, 144, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
}

.sidebar-menu ::v-deep .el-submenu .el-menu-item.is-active {
  background: rgba(24, 144, 255, 0.15);
  color: #fff;
}

/* 覆盖Element UI子菜单面板的默认白色背景 */
.sidebar-menu ::v-deep .el-menu--inline .el-menu-item,
.sidebar-menu ::v-deep .el-menu--popup {
  background-color: #001529 !important;
}

.sidebar-menu ::v-deep .el-submenu .el-menu {
  background-color: #001529 !important;
}

.sidebar-menu ::v-deep .el-menu--inline {
  background-color: #001529 !important;
}

.app-container ::v-deep .el-container > .el-container {
  margin-left: 200px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0;
  flex-shrink: 0;
  height: 64px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 24px;
}

.header-content h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  display: flex;
  align-items: center;
}

.header-content h3::before {
  content: "";
  width: 3px;
  height: 16px;
  background: #1890ff;
  margin-right: 12px;
  border-radius: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  cursor: pointer;
  color: #595959;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 14px;
}

.user-info:hover {
  background: #f5f5f5;
  color: #1890ff;
}

.user-info i {
  font-size: 16px;
}

.main-content {
  padding: 24px;
  background: #f0f2f5;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  height: 0;
  min-height: 0;
  transition: all 0.3s ease;
  /* 确保内容区域可以独立滚动 */
  -webkit-overflow-scrolling: touch;
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.show {
    transform: translateX(0);
  }

  .app-container ::v-deep .el-container > .el-container {
    margin-left: 0;
  }

  .main-content {
    padding: 16px;
  }

  .header-content {
    padding: 0 16px;
  }
}

/* 动画增强 */
.sidebar-menu ::v-deep .el-menu-item,
.sidebar-menu ::v-deep .el-submenu__title {
  animation: fadeInUp 0.5s ease-out forwards;
}

.sidebar-menu ::v-deep .el-menu-item:nth-child(1) {
  animation-delay: 0.1s;
}
.sidebar-menu ::v-deep .el-menu-item:nth-child(2) {
  animation-delay: 0.2s;
}
.sidebar-menu ::v-deep .el-menu-item:nth-child(3) {
  animation-delay: 0.3s;
}
.sidebar-menu ::v-deep .el-menu-item:nth-child(4) {
  animation-delay: 0.4s;
}
.sidebar-menu ::v-deep .el-menu-item:nth-child(5) {
  animation-delay: 0.5s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
