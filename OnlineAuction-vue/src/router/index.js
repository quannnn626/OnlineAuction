import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
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
        path: "notice",
        name: "Notice",
        component: () => import("@/views/Notice.vue"),
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

export default router;
