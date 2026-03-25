import { setPassword } from "../../utils/mp-api";

Page({
  data: {
    password: "",
    confirmPassword: "",
    loading: false,
  },

  onInputPassword(e: WechatMiniprogram.Input) {
    this.setData({ password: e.detail.value });
  },

  onInputConfirmPassword(e: WechatMiniprogram.Input) {
    this.setData({ confirmPassword: e.detail.value });
  },

  async onSubmit() {
    const { password, confirmPassword } = this.data;
    if (!password || password.trim().length < 6) {
      wx.showToast({ title: "密码至少6位", icon: "none" });
      return;
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: "两次输入的密码不一致", icon: "none" });
      return;
    }

    this.setData({ loading: true });
    try {
      await setPassword(password);
      const app = getApp<IAppOption>();
      if (app.globalData.user) {
        app.globalData.user.needSetPassword = false;
        wx.setStorageSync("user", app.globalData.user);
      }
      wx.showToast({ title: "密码设置成功", icon: "success" });
      wx.switchTab({ url: "/pages/profile/profile" });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "设置失败";
      wx.showToast({ title: msg, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
});

