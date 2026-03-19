import Vue from "vue";
import VueRouter from "vue-router";
/**
 * 路由守卫
 */
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
        path: "special",
        name: "Special",
        component: () => import("@/views/Special.vue"),
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
        path: "seller/goods/add",
        name: "SellerGoodsAdd",
        component: () => import("@/views/seller/SellerGoodsAdd.vue"),
      },
      {
        path: "bid-history",
        name: "BidHistory",
        component: () => import("@/views/BidHistory.vue"),
      },
      {
        path: "apply-seller",
        name: "ApplySeller",
        component: () => import("@/views/ApplySeller.vue"),
        meta: { buyerOnly: true },
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
            path: "invoice",
            name: "ProfileInvoice",
            component: () => import("@/views/ProfileInvoice.vue"),
          },
        ],
      },
      {
        path: "message",
        name: "MessageCenter",
        component: () => import("@/views/MessageCenter.vue"),
        meta: { messageCenter: true },
      },
      {
        path: "message/service-orders",
        name: "ServiceOrders",
        component: () => import("@/views/ServiceOrders.vue"),
        meta: { serviceOrders: true },
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
        path: "admin/seller-audit",
        name: "AdminSellerAudit",
        component: () => import("@/views/admin/AdminSellerAudit.vue"),
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
        path: "admin/fee-record",
        name: "AdminFeeRecord",
        component: () => import("@/views/admin/AdminFeeRecord.vue"),
      },
      {
        path: "admin/invoice",
        name: "AdminInvoice",
        component: () => import("@/views/admin/AdminInvoice.vue"),
      },
      {
        path: "admin/recharge-apply",
        name: "AdminRechargeApply",
        component: () => import("@/views/admin/AdminRechargeApply.vue"),
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
      {
        path: "admin/special",
        name: "AdminSpecial",
        component: () => import("@/views/admin/AdminSpecial.vue"),
      },
      {
        path: "admin/goods-time",
        name: "AdminGoodsTime",
        component: () => import("@/views/admin/AdminGoodsTime.vue"),
      },
      {
        path: "admin/recommend",
        name: "AdminRecommend",
        component: () => import("@/views/admin/AdminRecommend.vue"),
      },
      {
        path: "admin/dashboard",
        name: "AdminDashboard",
        component: () => import("@/views/admin/AdminDashboard.vue"),
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

  // 卖方页面权限检查（如：我的商品、申请上架商品等）
  if (to.path === "/my-goods" || to.path.startsWith("/seller")) {
    if (!user.isSeller && !user.isAdmin && !user.isSuperAdmin) {
      next("/home");
      return;
    }
  }

  // 申请成为卖家：仅买家（role=1）可访问，已是卖家则跳转到我的商品
  if (to.path === "/apply-seller" && to.meta?.buyerOnly) {
    const roles = user.userRole
      ? String(user.userRole)
          .split(",")
          .map((r) => r.trim())
      : [];
    if (roles.includes("2")) {
      next("/my-goods");
      return;
    }
    if (!roles.includes("1")) {
      next("/home");
      return;
    }
  }

  // 竞拍公告页面权限检查（仅买方、卖方、超级管理员可查看）
  if (to.path === "/notice" && to.meta?.noticeView) {
    let canView =
      user.isBuyer === true ||
      user.isSeller === true ||
      user.isSuperAdmin === true;
    if (!canView && user.userRole) {
      const roles = String(user.userRole)
        .split(",")
        .map((r) => r.trim());
      canView =
        roles.includes("1") || roles.includes("2") || roles.includes("4");
    }
    if (!canView) {
      next("/home");
      return;
    }
  }

  // 消息中心：买方、卖方、管理员、超管、客服、拍卖师、财务、运营均可访问（会话可见性由后端控制）
  if (to.path === "/message" && to.meta?.messageCenter) {
    const roles = user.userRole
      ? String(user.userRole)
          .split(",")
          .map((r) => r.trim())
      : [];
    const canAccess = roles.some((r) =>
      ["1", "2", "3", "4", "5", "6", "7", "8"].includes(r),
    );
    if (!canAccess) {
      next("/home");
      return;
    }
  }

  // 客服查看用户订单页：仅客服可访问，且必须带 userId 参数（从会话内跳转）
  if (to.path === "/message/service-orders" && to.meta?.serviceOrders) {
    const roles = user.userRole
      ? String(user.userRole)
          .split(",")
          .map((r) => r.trim())
      : [];
    if (!roles.includes("6")) {
      next("/message");
      return;
    }
    if (!to.query.userId) {
      next("/message");
      return;
    }
  }

  // 留言板页面权限检查（买方 role=1、卖方 role=2 可访问）
  if (to.path === "/message-board") {
    const roles = user.userRole
      ? String(user.userRole)
          .split(",")
          .map((r) => r.trim())
      : [];
    if (!roles.includes("1") && !roles.includes("2")) {
      next("/home");
      return;
    }
  }

  next();
});

export default router;
