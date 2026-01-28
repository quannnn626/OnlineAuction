package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.dto.LoginDTO;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

/**
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionUserService extends IService<AuctionUser> {

    /**
     * 分页查询用户列表
     * @param currentUserId 当前登录用户ID，用于权限过滤
     */
    PageInfo<AuctionUser> getUserPage(Integer current, Integer size, String userName, Integer userRole, Integer userStatus, Long currentUserId);

    /**
     * 根据ID查询用户详情（不返回密码）
     */
    AuctionUser getUserByIdWithoutPassword(Long id);

    /**
     * 创建用户（需要传入当前登录用户ID，用于权限检查）
     * @param user 要创建的用户信息
     * @param currentUserId 当前登录用户ID
     * @return 创建的用户信息
     */
    AuctionUser createUser(AuctionUser user, Long currentUserId);

    /**
     * 更新用户信息（需要传入当前登录用户ID，用于权限检查）
     * @param id 要更新的用户ID
     * @param user 更新的用户信息
     * @param currentUserId 当前登录用户ID
     * @return 更新后的用户信息
     */
    AuctionUser updateUser(Long id, AuctionUser user, Long currentUserId);

    /**
     * 更新用户状态（禁用/恢复）
     */
    void updateUserStatus(Long id, Integer userStatus);

    /**
     * 买方用户申请成为卖方用户
     */
    void applySeller(Long id, String certificateFiles);

    /**
     * 卖方用户资质审核
     */
    void auditSeller(Long id, Integer auditStatus, String auditRemark, Long auditUserId);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long id);

    /**
     * 用户登录
     * @param userName 用户名
     * @param password 密码（明文）
     * @param loginIp 登录IP
     * @return 登录信息（不包含密码）
     */
    LoginDTO login(String userName, String password, String loginIp);
}
