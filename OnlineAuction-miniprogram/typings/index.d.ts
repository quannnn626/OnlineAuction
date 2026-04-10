/// <reference path="./types/index.d.ts" />

interface IAppOption {
  globalData: {
    userInfo?: WechatMiniprogram.UserInfo,
    baseUrl: string,
    uploadBase: string,
    sessionId: string,
    user?: {
      id: number,
      userName: string,
      nickName: string,
      avatar?: string,
      userRole: string,
      needSetPassword?: boolean,
      webLoginAllowed?: boolean,
      isBuyer?: boolean,
      isSeller?: boolean
    }
  }
  userInfoReadyCallback?: WechatMiniprogram.GetUserInfoSuccessCallback,
}