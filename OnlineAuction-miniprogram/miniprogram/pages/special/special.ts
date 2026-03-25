import { getSpecialList, SpecialItem } from "../../utils/mp-api";

Page({
  data: {
    list: [] as SpecialItem[],
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
    await this.loadList();
  },

  async loadList() {
    try {
      const list = await getSpecialList();
      this.setData({ list: list || [] });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "加载失败";
      wx.showToast({ title: msg, icon: "none" });
    }
  },

  toSpecialGoods(e: WechatMiniprogram.BaseEvent) {
    const id = e.currentTarget.dataset.id as number;
    const name = e.currentTarget.dataset.name as string;
    wx.navigateTo({ url: `/pages/special-goods/special-goods?id=${id}&name=${encodeURIComponent(name || "")}` });
  },
});
