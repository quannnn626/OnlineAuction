import { getHomeGoods, GoodsItem } from "../../utils/mp-api";

Page({
  data: {
    keyword: "",
    list: [] as GoodsItem[],
    loading: false,
  },

  onShow() {
    this.ensureLoginAndLoad();
  },

  onInputKeyword(e: WechatMiniprogram.Input) {
    this.setData({ keyword: e.detail.value });
  },

  async onSearch() {
    await this.loadList();
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
    this.setData({ loading: true });
    try {
      const pageInfo = await getHomeGoods(1, 20, this.data.keyword || "");
      this.setData({ list: pageInfo.list || [] });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "加载失败";
      wx.showToast({ title: msg, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
});
