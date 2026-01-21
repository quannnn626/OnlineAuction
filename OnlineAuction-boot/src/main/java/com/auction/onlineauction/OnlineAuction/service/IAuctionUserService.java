package com.auction.onlineauction.OnlineAuction.service;

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
     */
    PageInfo<AuctionUser> getUserPage(Integer current, Integer size, String userName, Integer userRole, Integer userStatus);

    /**
     * 根据ID查询用户详情（不返回密码）
     */
    AuctionUser getUserByIdWithoutPassword(Long id);

    /**
     * 创建用户
     */
    AuctionUser createUser(AuctionUser user);

    /**
     * 更新用户信息
     */
    AuctionUser updateUser(Long id, AuctionUser user);

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
}
