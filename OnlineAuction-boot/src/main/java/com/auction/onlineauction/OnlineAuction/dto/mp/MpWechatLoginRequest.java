package com.auction.onlineauction.OnlineAuction.dto.mp;

import lombok.Data;

/**
 * 小程序微信登录请求
 */
@Data
public class MpWechatLoginRequest {
    /**
     * 小程序 wx.login 获取的 code
     */
    private String code;

    /**
     * 微信授权手机号
     */
    private String phone;

    /**
     * 微信昵称
     */
    private String nickName;

    /**
     * 微信头像URL
     */
    private String avatar;
}
