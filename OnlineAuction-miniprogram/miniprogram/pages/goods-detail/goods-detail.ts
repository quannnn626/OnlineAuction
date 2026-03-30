import { getGoodsDetail } from "../../utils/mp-api";

Page({
  data: {
    id: 0,
    detail: {} as Record<string, unknown>,
    uploadBase: "",
  },

  onLoad(query: Record<string, string>) {
    const id = Number(query.id || 0);
    const app = getApp<IAppOption>();
    this.setData({ id, uploadBase: app.globalData.uploadBase });
    this.loadDetail();
  },

  async loadDetail() {
    if (!this.data.id) return;
    try {
      const detail = await getGoodsDetail(this.data.id);
      this.setData({ detail: detail || {} });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "加载失败";
      wx.showToast({ title: msg, icon: "none" });
    }
  },
});

