/// <reference path="./types/index.d.ts" />

interface IAppOption {
  globalData: {
    userInfo?: WechatMiniprogram.UserInfo,
    baseUrl: string,
    sessionId: string,
    user?: {
      id: number,
      userName: string,
      nickName: string,
      avatar?: string,
      userRole: string
    }
  }
  userInfoReadyCallback?: WechatMiniprogram.GetUserInfoSuccessCallback,
}