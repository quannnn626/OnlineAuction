package com.auction.onlineauction.OnlineAuction.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录响应DTO（不包含密码等敏感信息）
 */
@Data
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户角色（多个角色用逗号分隔）：1=买方 2=卖方 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营
     */
    private String userRole;

    /**
     * 角色列表（解析后的角色数组）
     */
    private List<String> roles;

    /**
     * 是否管理员
     */
    private Boolean isAdmin;

    /**
     * 是否超级管理员
     */
    private Boolean isSuperAdmin;

    /**
     * 是否买方
     */
    private Boolean isBuyer;

    /**
     * 是否卖方
     */
    private Boolean isSeller;

    /**
     * 账号状态：0=正常 1=禁用
     */
    private Integer userStatus;

    /**
     * 是否需要设置密码（小程序首次自动注册时，password 为空）
     */
    private Boolean needSetPassword;

    /**
     * 是否允许使用网页端登录（需已设置密码且昵称非小程序默认占位）
     */
    private Boolean webLoginAllowed;
}
