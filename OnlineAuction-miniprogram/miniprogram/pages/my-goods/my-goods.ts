import { getMyGoods, GoodsItem } from "../../utils/mp-api";

Page({
  data: {
    uploadBase: "",
    list: [] as GoodsItem[],
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
    this.setData({ uploadBase: app.globalData.uploadBase });
    await this.loadList();
  },

  async loadList() {
    try {
      const pageInfo = await getMyGoods(1, 20);
      this.setData({ list: pageInfo.list || [] });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "加载失败";
      wx.showToast({ title: msg, icon: "none" });
    }
  },

  toGoodsDetail(e: WechatMiniprogram.BaseEvent) {
    const id = Number(e.currentTarget.dataset.id || 0);
    if (!id) return;
    wx.navigateTo({ url: `/pages/goods-detail/goods-detail?id=${id}` });
  },
});
