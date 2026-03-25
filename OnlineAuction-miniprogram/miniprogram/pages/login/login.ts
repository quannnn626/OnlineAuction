import { wechatLogin } from "../../utils/mp-api";

Page({
  data: {
    phone: "",
    nickName: "",
    avatar: "",
    code: "",
    loading: false,
  },

  onLoad() {
    const app = getApp<IAppOption>();
    if (app.globalData.user) {
      wx.switchTab({ url: "/pages/home/home" });
    }
  },

  onInputPhone(e: WechatMiniprogram.Input) {
    this.setData({ phone: e.detail.value.trim() });
  },

  onInputNickName(e: WechatMiniprogram.Input) {
    this.setData({ nickName: e.detail.value.trim() });
  },

  onInputAvatar(e: WechatMiniprogram.Input) {
    this.setData({ avatar: e.detail.value.trim() });
  },

  fetchLoginCode(): Promise<string> {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res.code) resolve(res.code);
          else reject(new Error("获取微信code失败"));
        },
        fail: () => reject(new Error("调用wx.login失败")),
      });
    });
  },

  async onSubmit() {
    if (!/^1\d{10}$/.test(this.data.phone)) {
      wx.showToast({ title: "请输入11位手机号", icon: "none" });
      return;
    }

    this.setData({ loading: true });
    try {
      const code = await this.fetchLoginCode();
      const resp = await wechatLogin({
        code,
        phone: this.data.phone,
        nickName: this.data.nickName,
        avatar: this.data.avatar,
      });
      const app = getApp<IAppOption>();
      app.globalData.user = resp.user;
      wx.setStorageSync("user", resp.user);
      wx.showToast({ title: "登录成功", icon: "success" });
      wx.switchTab({ url: "/pages/home/home" });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "登录失败";
      wx.showToast({ title: msg, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
});
