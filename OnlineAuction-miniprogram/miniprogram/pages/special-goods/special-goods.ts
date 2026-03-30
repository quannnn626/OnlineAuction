import { getSpecialGoods } from "../../utils/mp-api";

interface SpecialGoodsItem {
  goodsId: number;
  goodsName: string;
  goodsDesc: string;
  currentHighestPrice?: number;
  basePrice?: number;
  files?: Array<{ filePath: string }>;
}

Page({
  data: {
    specialId: 0,
    specialName: "",
    uploadBase: "",
    list: [] as SpecialGoodsItem[],
  },

  onLoad(query: Record<string, string>) {
    const specialId = Number(query.id || 0);
    const specialName = decodeURIComponent(query.name || "");
    this.setData({ specialId, specialName });
    this.loadList();
  },

  async loadList() {
    if (!this.data.specialId) return;
    try {
      const app = getApp<IAppOption>();
      this.setData({ uploadBase: app.globalData.uploadBase });
      const raw = await getSpecialGoods(this.data.specialId);
      const list = (raw || []).map((item) => {
        const obj = item as Record<string, unknown>;
        return {
          goodsId: Number(obj.goodsId || obj.id || 0),
          goodsName: String(obj.goodsName || obj.name || "未命名商品"),
          goodsDesc: String(obj.goodsDesc || obj.description || ""),
          currentHighestPrice: Number(obj.currentHighestPrice || 0),
          basePrice: Number(obj.basePrice || 0),
          files: (obj.files as Array<{ filePath: string }>) || [],
        };
      });
      this.setData({ list });
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
