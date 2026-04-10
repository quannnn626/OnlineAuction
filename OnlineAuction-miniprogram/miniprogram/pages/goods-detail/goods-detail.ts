import {
  getBidRecordsByGoodsId,
  getGoodsDetail,
  submitBid as postBidApi,
} from "../../utils/mp-api";

type FileLike = { fileType?: string; filePath?: string; fileName?: string };

type StoredUser = NonNullable<IAppOption["globalData"]["user"]> & {
  isBuyer?: boolean;
  isSeller?: boolean;
};

function getFileExt(path: string): string {
  const clean = (path || "").split("?")[0];
  const i = clean.lastIndexOf(".");
  return i < 0 ? "" : clean.substring(i + 1).toLowerCase();
}

function isImageFile(f: FileLike): boolean {
  const type = (f.fileType || "").toLowerCase();
  if (type === "image") return true;
  const ext = getFileExt(f.filePath || f.fileName || "");
  return ["jpg", "jpeg", "png", "gif", "webp", "bmp"].includes(ext);
}

function isVideoFile(f: FileLike): boolean {
  const type = (f.fileType || "").toLowerCase();
  if (type === "video") return true;
  const ext = getFileExt(f.filePath || f.fileName || "");
  return ["mp4", "webm", "ogg", "mov", "m4v"].includes(ext);
}

function absMediaUrl(uploadBase: string, path: string): string {
  if (!path) return "";
  if (/^https?:\/\//i.test(path)) return path;
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${uploadBase}${p}`;
}

function parseLegacyMedia(
  goodsImg: unknown,
  uploadBase: string,
  onlyImage: boolean,
): string[] {
  if (typeof goodsImg !== "string" || !goodsImg.trim()) return [];
  const list = goodsImg
    .split(",")
    .map((v) => absMediaUrl(uploadBase, v.trim()))
    .filter((v) => !!v);
  if (!onlyImage) return list;
  return list.filter((u) => isImageFile({ filePath: u }));
}

function buildMediaFromDetail(
  detail: Record<string, unknown>,
  uploadBase: string,
): { imageUrls: string[]; videoUrls: string[] } {
  const files = Array.isArray(detail.files)
    ? (detail.files as FileLike[])
    : [];

  const fromFilesImages = files
    .filter((f) => f && f.filePath && isImageFile(f))
    .map((f) => absMediaUrl(uploadBase, f.filePath as string));

  const fromFilesVideos = files
    .filter((f) => f && f.filePath && isVideoFile(f))
    .map((f) => absMediaUrl(uploadBase, f.filePath as string));

  const imageUrls =
    fromFilesImages.length > 0
      ? fromFilesImages
      : parseLegacyMedia(detail.goodsImg, uploadBase, true);

  let videoUrls = fromFilesVideos;
  if (videoUrls.length === 0) {
    const legacy = parseLegacyMedia(detail.goodsImg, uploadBase, false);
    videoUrls = legacy.filter((u) => isVideoFile({ filePath: u }));
  }

  return { imageUrls, videoUrls };
}

function num(v: unknown): number {
  if (v == null || v === "") return 0;
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function computeMinBidPrice(detail: Record<string, unknown>): number {
  const cur = detail.currentHighestPrice;
  if (cur != null && cur !== "") {
    return num(cur) + num(detail.addPrice);
  }
  return num(detail.basePrice);
}

function parseServerTimeToMs(v: unknown): number | null {
  if (v == null) return null;
  if (typeof v === "string") {
    const t = Date.parse(v.replace(" ", "T"));
    return Number.isNaN(t) ? null : t;
  }
  if (Array.isArray(v) && v.length >= 3) {
    const y = Number(v[0]);
    const m = Number(v[1]) - 1;
    const d = Number(v[2]);
    const h = v.length > 3 ? Number(v[3]) : 0;
    const min = v.length > 4 ? Number(v[4]) : 0;
    const sec = v.length > 5 ? Number(v[5]) : 0;
    return new Date(y, m, d, h, min, sec).getTime();
  }
  return null;
}

function goodsStatusText(status: unknown): string {
  const m: Record<number, string> = {
    0: "未开始",
    1: "竞拍中",
    2: "已成交",
    3: "已流拍",
  };
  const s = Number(status);
  return m[s] ?? String(status ?? "-");
}

function formatDetailDateTime(v: unknown): string {
  if (v == null) return "-";
  if (typeof v === "string") {
    return v.length >= 16 ? v.slice(0, 16).replace("T", " ") : v;
  }
  if (Array.isArray(v) && v.length >= 5) {
    const y = v[0];
    const mo = String(v[1]).padStart(2, "0");
    const d = String(v[2]).padStart(2, "0");
    const h = String(v[3]).padStart(2, "0");
    const mi = String(v[4]).padStart(2, "0");
    return `${y}-${mo}-${d} ${h}:${mi}`;
  }
  return "-";
}

function userHasBidRole(user: StoredUser | undefined): boolean {
  if (!user) return false;
  if (user.isBuyer || user.isSeller) return true;
  const roles = String(user.userRole || "")
    .split(",")
    .map((x) => x.trim());
  return roles.includes("1") || roles.includes("2");
}

function explainNoBid(
  user: StoredUser | undefined,
  detail: Record<string, unknown>,
): string {
  if (!user) return "请先登录后再出价";
  if (!userHasBidRole(user)) return "当前账号无买方/卖方角色，无法参与竞拍";
  const sellerId = num(detail.sellerId);
  if (sellerId > 0 && user.id === sellerId) return "不能竞拍自己发布的商品";
  const now = Date.now();
  const start = parseServerTimeToMs(detail.startTime);
  const end = parseServerTimeToMs(detail.endTime);
  if (start != null && now < start) return "竞拍尚未开始";
  if (end != null && now > end) return "竞拍已结束";
  if (Number(detail.goodsStatus) !== 1) return "商品当前不在竞拍中";
  return "暂时无法出价";
}

function computeCanBid(
  user: StoredUser | undefined,
  detail: Record<string, unknown>,
): boolean {
  if (!user || !userHasBidRole(user)) return false;
  const sellerId = num(detail.sellerId);
  if (sellerId > 0 && user.id === sellerId) return false;
  if (Number(detail.goodsStatus) !== 1) return false;
  const now = Date.now();
  const start = parseServerTimeToMs(detail.startTime);
  const end = parseServerTimeToMs(detail.endTime);
  if (start != null && now < start) return false;
  if (end != null && now > end) return false;
  return true;
}

function formatBidTime(v: unknown): string {
  if (v == null) return "";
  if (typeof v === "string") {
    return v.length >= 16 ? v.slice(5, 16).replace("T", " ") : v;
  }
  if (Array.isArray(v) && v.length >= 5) {
    const mo = String(v[1]).padStart(2, "0");
    const d = String(v[2]).padStart(2, "0");
    const h = String(v[3]).padStart(2, "0");
    const mi = String(v[4]).padStart(2, "0");
    return `${mo}-${d} ${h}:${mi}`;
  }
  return "";
}

Page({
  data: {
    id: 0,
    detail: {} as Record<string, unknown>,
    uploadBase: "",
    imageUrls: [] as string[],
    videoUrls: [] as string[],
    minBidPrice: 0,
    minBidPriceText: "0.00",
    addPriceText: "0.00",
    goodsStatusText: "",
    canBid: false,
    bidDisabledReason: "",
    bidInput: "",
    bidding: false,
    bidRecordsDisplay: [] as Array<{
      id: number;
      buyerName: string;
      bidPriceText: string;
      bidTimeText: string;
      isHighest: boolean;
    }>,
    recordsLoading: false,
    startTimeText: "",
    endTimeText: "",
  },

  onLoad(query: Record<string, string>) {
    const id = Number(query.id || 0);
    const app = getApp<IAppOption>();
    this.setData({ id, uploadBase: app.globalData.uploadBase });
    this.loadDetail();
  },

  onShow() {
    if (this.data.id && this.data.detail && (this.data.detail as { id?: number }).id) {
      this.applyDerivedFromDetail(false);
    }
  },

  syncStoredUser(): StoredUser | undefined {
    const app = getApp<IAppOption>();
    const user = app.globalData.user || wx.getStorageSync("user");
    if (user) {
      app.globalData.user = user;
      return user as StoredUser;
    }
    return undefined;
  },

  applyDerivedFromDetail(resetBidInput: boolean) {
    const detail = this.data.detail;
    if (!detail || !this.data.id) return;
    const user = this.syncStoredUser();
    const minBid = computeMinBidPrice(detail);
    const canBid = computeCanBid(user, detail);
    const patch: Record<string, unknown> = {
      minBidPrice: minBid,
      minBidPriceText: minBid.toFixed(2),
      addPriceText: num(detail.addPrice).toFixed(2),
      goodsStatusText: goodsStatusText(detail.goodsStatus),
      startTimeText: formatDetailDateTime(detail.startTime),
      endTimeText: formatDetailDateTime(detail.endTime),
      canBid,
      bidDisabledReason: canBid ? "" : explainNoBid(user, detail),
    };
    if (resetBidInput) {
      patch.bidInput = minBid.toFixed(2);
    }
    this.setData(patch);
  },

  async loadDetail() {
    if (!this.data.id) return;
    try {
      const detail = (await getGoodsDetail(this.data.id)) || {};
      const { imageUrls, videoUrls } = buildMediaFromDetail(
        detail as Record<string, unknown>,
        this.data.uploadBase,
      );
      this.setData({ detail, imageUrls, videoUrls });
      this.applyDerivedFromDetail(true);
      await this.loadBidRecords();
    } catch (err) {
      const msg = err instanceof Error ? err.message : "加载失败";
      wx.showToast({ title: msg, icon: "none" });
    }
  },

  async loadBidRecords() {
    if (!this.data.id) return;
    this.setData({ recordsLoading: true });
    try {
      const rows = await getBidRecordsByGoodsId(this.data.id, 20);
      const list = Array.isArray(rows) ? rows : [];
      const bidRecordsDisplay = list.map((r, idx) => ({
        id: typeof r.id === "number" ? r.id : idx,
        buyerName: r.buyerName || r.buyer_name || "买家",
        bidPriceText: num(r.bidPrice ?? r.bid_price).toFixed(2),
        bidTimeText: formatBidTime(r.bidTime ?? r.bid_time),
        isHighest: (r.isHighest ?? r.is_highest) === 1,
      }));
      this.setData({ bidRecordsDisplay });
    } catch {
      this.setData({ bidRecordsDisplay: [] });
    } finally {
      this.setData({ recordsLoading: false });
    }
  },

  onBidInput(e: WechatMiniprogram.Input) {
    this.setData({ bidInput: e.detail.value || "" });
  },

  async submitBid() {
    if (!this.data.canBid || this.data.bidding) return;
    const raw = (this.data.bidInput || "").trim();
    const price = parseFloat(raw);
    if (Number.isNaN(price) || price <= 0) {
      wx.showToast({ title: "请输入有效出价金额", icon: "none" });
      return;
    }
    const minBid = this.data.minBidPrice;
    if (price + 1e-9 < minBid) {
      wx.showToast({
        title: `出价不能低于 ${minBid.toFixed(2)} 元`,
        icon: "none",
      });
      return;
    }
    this.setData({ bidding: true });
    try {
      await postBidApi(this.data.id, price);
      wx.showToast({ title: "出价成功", icon: "success" });
      await this.loadDetail();
    } catch (err) {
      const msg = err instanceof Error ? err.message : "出价失败";
      wx.showToast({ title: msg, icon: "none", duration: 2500 });
    } finally {
      this.setData({ bidding: false });
    }
  },

  quickBid() {
    if (!this.data.canBid) return;
    this.setData({ bidInput: this.data.minBidPriceText }, () => {
      void this.submitBid();
    });
  },
});
