import { getSpecialGoods } from "../../utils/mp-api";

Page({
  data: {
    specialId: 0,
    specialName: "",
    list: [] as Array<Record<string, unknown>>,
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
      const list = await getSpecialGoods(this.data.specialId);
      this.setData({ list: list || [] });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "加载失败";
      wx.showToast({ title: msg, icon: "none" });
    }
  },
});
