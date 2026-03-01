import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
  },
  {
    path: "/",
    name: "Index",
    component: () => import("@/views/Index.vue"),
    children: [
      {
        path: "",
        redirect: "/home",
      },
      {
        path: "home",
        name: "Home",
        component: () => import("@/views/Home.vue"),
      },
      {
        path: "goods",
        name: "Goods",
        component: () => import("@/views/Goods.vue"),
      },
      {
        path: "goods-detail",
        name: "GoodsDetail",
        component: () => import("@/views/GoodsDetail.vue"),
      },
      {
        path: "search-results",
        name: "SearchResults",
        component: () => import("@/views/SearchResults.vue"),
      },
      {
        path: "my-goods",
        name: "MyGoods",
        component: () => import("@/views/MyGoods.vue"),
      },
      {
        path: "bid-history",
        name: "BidHistory",
        component: () => import("@/views/BidHistory.vue"),
      },
      {
        path: "notice",
        name: "Notice",
        component: () => import("@/views/Notice.vue"),
        meta: { noticeView: true },
      },
      {
        path: "message-board",
        name: "MessageBoard",
        component: () => import("@/views/MessageBoard.vue"),
        meta: { roles: ["1", "2"] }, // 仅买方和卖方可以访问
      },
      {
        path: "profile",
        name: "Profile",
        component: () => import("@/views/Profile.vue"),
        children: [
          {
            path: "order",
            name: "ProfileOrder",
            component: () => import("@/views/ProfileOrder.vue"),
          },
          {
            path: "deposit",
            name: "ProfileDeposit",
            component: () => import("@/views/ProfileDeposit.vue"),
          },
          {
            path: "message",
            name: "ProfileMessage",
            component: () => import("@/views/ProfileMessage.vue"),
          },
        ],
      },
      {
        path: "admin",
        redirect: "/admin/profile",
      },
      {
        path: "admin/profile",
        name: "AdminProfile",
        component: () => import("@/views/admin/AdminProfile.vue"),
      },
      {
        path: "admin/user",
        name: "AdminUser",
        component: () => import("@/views/admin/AdminUser.vue"),
      },
      {
        path: "admin/category",
        name: "AdminCategory",
        component: () => import("@/views/admin/AdminCategory.vue"),
      },
      {
        path: "admin/goods",
        name: "AdminGoods",
        component: () => import("@/views/admin/AdminGoods.vue"),
      },
      {
        path: "admin/history",
        name: "AdminHistory",
        component: () => import("@/views/admin/AdminHistory.vue"),
      },
      {
        path: "admin/order",
        name: "AdminOrder",
        component: () => import("@/views/admin/AdminOrder.vue"),
      },
      {
        path: "admin/deposit",
        name: "AdminDeposit",
        component: () => import("@/views/admin/AdminDeposit.vue"),
      },
      {
        path: "admin/message",
        name: "AdminMessage",
        component: () => import("@/views/admin/AdminMessage.vue"),
      },
      {
        path: "admin/settings",
        name: "AdminSettings",
        component: () => import("@/views/admin/AdminSettings.vue"),
      },
      {
        path: "admin/settings/banner",
        name: "AdminSettingsBanner",
        component: () => import("@/views/admin/AdminSettingsBanner.vue"),
      },
      {
        path: "admin/settings/notice",
        name: "AdminSettingsNotice",
        component: () => import("@/views/admin/AdminSettingsNotice.vue"),
      },
    ],
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

// 路由守卫：根据登录状态和角色进行路由跳转
router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem("userInfo");
  const isLogin = !!userInfo;
  
  // 如果访问登录页，已登录则跳转到首页
  if (to.path === "/login") {
    if (isLogin) {
      const user = JSON.parse(userInfo);
      if (user.isAdmin) {
        next("/admin/profile");
      } else if (user.isSeller) {
        next("/my-goods");
      } else {
        next("/home");
      }
    } else {
      next();
    }
    return;
  }
  
  // 如果未登录，跳转到登录页
  if (!isLogin) {
    next("/login");
    return;
  }
  
  // 已登录，检查权限
  const user = JSON.parse(userInfo);
  
  // 管理员页面权限检查
  if (to.path.startsWith("/admin")) {
    if (!user.isAdmin && !user.isSuperAdmin) {
      // 非管理员访问后台，跳转到首页
      next("/home");
      return;
    }
  }
  
  // 卖方页面权限检查（如：我的商品、发布商品等）
  if (to.path === "/my-goods" || to.path.includes("/seller")) {
    if (!user.isSeller && !user.isAdmin) {
      // 非卖方用户访问卖方页面，跳转到首页
      next("/home");
      return;
    }
  }
  
  // 竞拍公告页面权限检查（仅买方、卖方、超级管理员可查看）
  if (to.path === "/notice" && to.meta?.noticeView) {
    let canView = user.isBuyer === true || user.isSeller === true || user.isSuperAdmin === true;
    if (!canView && user.userRole) {
      const roles = String(user.userRole).split(",").map(r => r.trim());
      canView = roles.includes("1") || roles.includes("2") || roles.includes("4");
    }
    if (!canView) {
      next("/home");
      return;
    }
  }

  // 留言板页面权限检查（仅买方和卖方可以访问）
  if (to.path === "/message-board") {
    // 检查用户是否有买方或卖方角色
    // 优先使用 isBuyer 和 isSeller 属性
    let hasPermission = false;
    if (user.isBuyer === true || user.isSeller === true) {
      hasPermission = true;
    } else if (user.userRole) {
      // 如果 isBuyer/isSeller 未设置，根据 userRole 判断
      // userRole 格式可能是 "1" 或 "1,2" 等（逗号分隔）
      const roles = String(user.userRole).split(",").map(r => r.trim());
      hasPermission = roles.includes("1") || roles.includes("2");
    }
    
    if (!hasPermission) {
      next("/home");
      return;
    }
  }
  
  next();
});

export default router;
