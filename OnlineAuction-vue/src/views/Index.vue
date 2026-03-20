<template>
  <div class="app-container">
    <el-container>
      <!-- 左侧菜单 -->
      <el-aside width="280px" class="sidebar">
        <div class="logo">
          <div class="logo-icon">◎</div>
          <span class="logo-text">拍卖系统</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          @select="handleMenuSelect"
          background-color="transparent"
          text-color="#94a3b8"
          active-text-color="#f8fafc"
        >
          <template v-for="menu in menuTree">
            <el-menu-item
              v-if="!menu.children || menu.children.length === 0"
              :key="'item-' + menu.id"
              :index="getMenuPath(menu)"
            >
              <i :class="menu.menuIcon || 'el-icon-menu'"></i>
              <span slot="title">{{ menu.menuName }}</span>
            </el-menu-item>
            <el-submenu v-else :key="'sub-' + menu.id" :index="String(menu.id)">
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
                  <el-dropdown-item command="profile"
                    >个人中心</el-dropdown-item
                  >
                  <el-dropdown-item divided command="logout"
                    >退出登录</el-dropdown-item
                  >
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
import { logout, getCurrentUser } from "@/api/auth";

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
      sessionCheckTimer: null,
    };
  },
  mounted() {
    this.loadUserInfo();
    this.loadMenus();
    this.setActiveMenu();
    this.updatePageTitle();
    // 有登录态时：启动时校验 session，防止服务重启后仍显示已登录
    this.validateSessionOnMount();
    // 定期校验 session（每 60 秒），服务重启后尽快检测并跳转登录
    this.startSessionCheckTimer();
  },
  beforeDestroy() {
    this.clearSessionCheckTimer();
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
        // 前端双重保护：仅买方(1)、卖方(2)可看到留言板，管理员等角色隐藏
        let menuTree = data || [];
        const userInfo = localStorage.getItem("userInfo");
        let user = null;
        if (userInfo) {
          try {
            user = JSON.parse(userInfo);
            const roles = user.userRole
              ? String(user.userRole)
                  .split(",")
                  .map((r) => r.trim())
              : [];
            const isEndUser = roles.includes("1") || roles.includes("2");
            if (!isEndUser) {
              // 非买方/卖方：过滤掉留言板菜单
              menuTree = this.filterMenu(menuTree, (menu) => {
                return menu.id !== 8 && menu.menuPath !== "/message-board";
              });
            }

            // 买方/卖方端兜底菜单：补充“拍卖专场”入口（避免后端菜单未配置导致看不到）
            if (isEndUser && !this.menuExists(menuTree, "/special")) {
              menuTree.push({
                id: 99990,
                menuName: "拍卖专场",
                menuPath: "/special",
                menuIcon: "el-icon-collection-tag",
                children: [],
              });
            }
          } catch (e) {
            // 忽略解析错误
          }
        }

        // 买方/卖方端兜底菜单：补充“历史竞拍”入口（避免后端菜单未配置导致看不到）
        if (
          user &&
          !user.isAdmin &&
          !user.isSuperAdmin &&
          !this.menuExists(menuTree, "/bid-history")
        ) {
          menuTree.push({
            id: 99991,
            menuName: "历史竞拍",
            menuPath: "/bid-history",
            menuIcon: "el-icon-time",
            children: [],
          });
        }
        // 卖方兜底菜单：补充“我的商品”入口（卖方独有）
        if (user && user.isSeller && !this.menuExists(menuTree, "/my-goods")) {
          menuTree.push({
            id: 99992,
            menuName: "我的商品",
            menuPath: "/my-goods",
            menuIcon: "el-icon-goods",
            children: [],
          });
        }
        // 客服/管理员兜底菜单：工单入口
        if (user) {
          const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
          const canWorkOrder = roles.includes("6") || roles.includes("3");
          if (canWorkOrder && !this.menuExists(menuTree, "/work-order")) {
            menuTree.push({
              id: 99995,
              menuName: roles.includes("3") ? "工单复核" : "工单处理",
              menuPath: "/work-order",
              menuIcon: "el-icon-tickets",
              children: [],
            });
          }
        }
        // 风控兜底菜单：风控中心入口
        if (user) {
          const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
          const canRisk = roles.includes("9");
          if (canRisk && !this.menuExists(menuTree, "/risk-dashboard")) {
            menuTree.push({
              id: 99996,
              menuName: "风控中心",
              menuPath: "/risk-dashboard",
              menuIcon: "el-icon-warning",
              children: [],
            });
          }
        }
        // 后台角色（管理员、超级管理员、拍卖师、客服、财务、运营）若无“个人中心/个人信息”菜单，则补充入口到 /admin/profile
        if (user) {
          const roles = user.userRole
            ? String(user.userRole)
                .split(",")
                .map((r) => r.trim())
            : [];
          const isStaff = [3, 4, 5, 6, 7, 8].some((r) =>
            roles.includes(String(r)),
          );
          const hasProfileMenu =
            this.menuExists(menuTree, "/admin/profile") ||
            this.menuExists(menuTree, "/profile");
          if (isStaff && !hasProfileMenu) {
            menuTree.push({
              id: 99993,
              menuName: "个人信息",
              menuPath: "/admin/profile",
              menuIcon: "el-icon-user",
              children: [],
            });
          }
        }
        // 拍卖师(5)、客服(6)、财务(7) 不显示系统设置及其子功能（轮播图管理、竞拍公告管理）
        if (user) {
          const roles = user.userRole
            ? String(user.userRole)
                .split(",")
                .map((r) => r.trim())
            : [];
          const onlyStaffNoAdmin =
            [5, 6, 7, 9].some((r) => roles.includes(String(r))) &&
            ![3, 4].some((r) => roles.includes(String(r)));
          if (onlyStaffNoAdmin) {
            menuTree = this.filterMenu(menuTree, (m) => {
              const path = (m.menuPath || "").trim();
              return (
                path !== "/admin/settings" &&
                path !== "admin/settings" &&
                !path.startsWith("/admin/settings/") &&
                !path.startsWith("admin/settings/")
              );
            });
          }
        }
        // 过滤掉“商品申请”菜单（改为在“我的商品”页面右上角的按钮入口）
        menuTree = menuTree.filter((m) => m.menuPath !== "/seller/goods/add");
        // 将「拍卖商品」菜单名统一显示为「拍品列表」
        menuTree = menuTree.map((m) => {
          if (m.menuPath === "/goods" && m.menuName === "拍卖商品") {
            return { ...m, menuName: "拍品列表" };
          }
          return m;
        });
        // 个人中心：点击直接进入 /profile 默认展示个人信息，子功能仍在个人中心页内 tab 中
        menuTree = menuTree.map((m) => {
          const path = (m.menuPath || "").trim();
          const isProfile = path === "/profile" || path === "profile";
          if (isProfile && m.children && m.children.length > 0) {
            return { ...m, children: [] };
          }
          return m;
        });

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
      return menus.filter(filterFn).map((menu) => {
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
    menuExists(menus, targetPath) {
      if (!menus || menus.length === 0) return false;
      for (const menu of menus) {
        const path = this.getMenuPath(menu);
        if (path === targetPath) return true;
        if (menu.children && menu.children.length > 0) {
          if (this.menuExists(menu.children, targetPath)) return true;
        }
      }
      return false;
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
    /** 启动时校验 session，服务重启后 session 已失效时由 request 拦截器清除并跳转登录 */
    validateSessionOnMount() {
      if (!localStorage.getItem("userInfo")) return;
      getCurrentUser().catch(() => {
        this.clearSessionCheckTimer();
      });
    },
    /** 定期校验 session，服务重启后尽快检测并跳转登录 */
    startSessionCheckTimer() {
      this.clearSessionCheckTimer();
      this.sessionCheckTimer = setInterval(() => {
        if (!localStorage.getItem("userInfo")) {
          this.clearSessionCheckTimer();
          return;
        }
        getCurrentUser().catch(() => {
          this.clearSessionCheckTimer();
        });
      }, 60000);
    },
    clearSessionCheckTimer() {
      if (this.sessionCheckTimer) {
        clearInterval(this.sessionCheckTimer);
        this.sessionCheckTimer = null;
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
  background: #f1f5f9;
}

.app-container ::v-deep .el-container {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(15, 23, 42, 0.25);
  border-right: 1px solid rgba(148, 163, 184, 0.08);
  transition: all 0.25s ease;
}

.logo {
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  flex-shrink: 0;
  padding: 0 24px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  position: relative;
}

.logo-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #c9a227 0%, #e8c547 50%, #c9a227 100%);
  border-radius: 12px;
  font-size: 24px;
  color: #0f172a;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(201, 162, 39, 0.35);
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #f8fafc;
  letter-spacing: 0.5px;
}

.sidebar-menu {
  border: none !important;
  background: transparent !important;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 16px;
}

.sidebar-menu ::v-deep .el-menu-item,
.sidebar-menu ::v-deep .el-submenu__title {
  color: #94a3b8 !important;
  font-size: 16px;
  font-weight: 500;
  margin: 4px 0;
  border-radius: 12px;
  height: 52px;
  line-height: 52px;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.sidebar-menu ::v-deep .el-menu-item i,
.sidebar-menu ::v-deep .el-submenu__title i {
  color: inherit;
  margin-right: 14px;
  font-size: 20px;
  opacity: 0.9;
}

.sidebar-menu ::v-deep .el-menu-item:hover,
.sidebar-menu ::v-deep .el-submenu__title:hover {
  background: rgba(148, 163, 184, 0.12) !important;
  color: #e2e8f0 !important;
}

.sidebar-menu ::v-deep .el-menu-item.is-active {
  background: linear-gradient(
    90deg,
    rgba(201, 162, 39, 0.2) 0%,
    rgba(201, 162, 39, 0.06) 100%
  ) !important;
  color: #f8fafc !important;
  border-left: 4px solid #c9a227;
  padding-left: 52px !important;
}

.sidebar-menu ::v-deep .el-menu-item.is-active i {
  color: #e8c547;
}

.sidebar-menu ::v-deep .el-submenu__title {
  padding-left: 20px !important;
}

.sidebar-menu ::v-deep .el-menu-item {
  padding-left: 54px !important;
  border-left: 4px solid transparent;
}

.sidebar-menu ::v-deep .el-submenu .el-menu-item {
  padding-left: 68px !important;
  height: 46px;
  line-height: 46px;
  font-size: 15px;
  color: #94a3b8 !important;
  border-left: 4px solid transparent;
}

.sidebar-menu ::v-deep .el-submenu .el-menu-item.is-active {
  border-left: 4px solid #c9a227;
  padding-left: 64px !important;
}

.sidebar-menu ::v-deep .el-submenu .el-menu-item:hover {
  background: rgba(148, 163, 184, 0.08) !important;
  color: #cbd5e1 !important;
}

.sidebar-menu ::v-deep .el-submenu .el-menu-item.is-active {
  background: rgba(201, 162, 39, 0.15) !important;
  color: #f8fafc !important;
}

.sidebar-menu ::v-deep .el-submenu__icon-arrow {
  color: inherit;
  font-size: 12px;
}

.sidebar-menu ::v-deep .el-menu--inline .el-menu-item,
.sidebar-menu ::v-deep .el-menu--popup {
  background-color: transparent !important;
}

.sidebar-menu ::v-deep .el-submenu .el-menu {
  background-color: transparent !important;
}

.sidebar-menu ::v-deep .el-menu--inline {
  background-color: transparent !important;
}

.app-container ::v-deep .el-container > .el-container {
  margin-left: 280px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.25s ease;
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
  background: linear-gradient(180deg, #c9a227 0%, #e8c547 100%);
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
  background: #f1f5f9;
  color: #b8860b;
}

.user-info i {
  font-size: 16px;
}

.main-content {
  padding: 24px;
  background: #f1f5f9;
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
</style>
