import { completeMpProfile } from "../../utils/mp-api";

Page({
  data: {
    nickName: "",
    password: "",
    confirmPassword: "",
    loading: false,
  },

  onInputNickName(e: WechatMiniprogram.Input) {
    this.setData({ nickName: e.detail.value.trim() });
  },

  onInputPassword(e: WechatMiniprogram.Input) {
    this.setData({ password: e.detail.value });
  },

  onInputConfirmPassword(e: WechatMiniprogram.Input) {
    this.setData({ confirmPassword: e.detail.value });
  },

  async onSubmit() {
    const { nickName, password, confirmPassword } = this.data;
    if (!nickName || nickName.length < 2) {
      wx.showToast({ title: "昵称至少2个字符", icon: "none" });
      return;
    }
    if (nickName.length > 50) {
      wx.showToast({ title: "昵称过长", icon: "none" });
      return;
    }
    if (nickName === "微信用户") {
      wx.showToast({ title: "请使用其他昵称", icon: "none" });
      return;
    }
    if (!password || password.trim().length < 6) {
      wx.showToast({ title: "密码至少6位", icon: "none" });
      return;
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: "两次密码不一致", icon: "none" });
      return;
    }

    this.setData({ loading: true });
    try {
      await completeMpProfile(nickName, password);
      const app = getApp<IAppOption>();
      if (app.globalData.user) {
        app.globalData.user.needSetPassword = false;
        app.globalData.user.webLoginAllowed = true;
        wx.setStorageSync("user", app.globalData.user);
      }
      wx.showToast({ title: "设置成功", icon: "success" });
      wx.switchTab({ url: "/pages/profile/profile" });
    } catch (err) {
      const msg = err instanceof Error ? err.message : "设置失败";
      wx.showToast({ title: msg, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
});
