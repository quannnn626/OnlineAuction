import { getMyProfile } from "../../utils/mp-api";

Page({
  data: {
    profile: {} as Record<string, unknown>,
  },

  onShow() {
    this.ensureLoginAndLoad();
  },

  async ensureLoginAndLoad() {
    const app = getApp<IAppOption>();
    const user = app.globalData.user || wx.getStorageSync("user");
    if (!user) {
      wx.reLaunch({ url: "/pages/login/login" });
      return;
    }
    app.globalData.user = user;
    await this.loadProfile();
  },

  async loadProfile() {
    try {
      const profile = await getMyProfile();
      this.setData({ profile });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "加载失败";
      wx.showToast({ title: msg, icon: "none" });
    }
  },

  logout() {
    const app = getApp<IAppOption>();
    app.globalData.user = undefined;
    app.globalData.sessionId = "";
    wx.removeStorageSync("user");
    wx.removeStorageSync("sessionId");
    wx.reLaunch({ url: "/pages/login/login" });
  },
});
