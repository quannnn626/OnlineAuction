import { wechatLogin } from "../../utils/mp-api";

Page({
  data: {
    phone: "",
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
      });
      const app = getApp<IAppOption>();
      app.globalData.user = resp.user;
      wx.setStorageSync("user", resp.user);

      if (resp.user.needSetPassword) {
        wx.showModal({
          title: "完善账号信息",
          content:
            "是否现在设置昵称与登录密码？设置后可使用网页端登录。",
          confirmText: "去设置",
          cancelText: "跳过",
          success: (r) => {
            if (r.confirm) {
              wx.navigateTo({ url: "/pages/set-password/set-password" });
            } else {
              wx.showToast({
                title: "已跳过，网页端将无法登录",
                icon: "none",
                duration: 2500,
              });
              setTimeout(() => {
                wx.switchTab({ url: "/pages/home/home" });
              }, 500);
            }
          },
        });
      } else {
        wx.showToast({ title: "登录成功", icon: "success" });
        wx.switchTab({ url: "/pages/home/home" });
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : "登录失败";
      wx.showToast({ title: msg, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
});
