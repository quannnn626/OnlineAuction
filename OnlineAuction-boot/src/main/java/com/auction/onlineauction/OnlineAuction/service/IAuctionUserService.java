package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.dto.LoginDTO;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

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
     * 卖家资质申请列表（后台审核用）
     * 仅返回 seller_audit_status in (1,2,3) 的用户
     */
    PageInfo<AuctionUser> getSellerAuditPage(Integer current, Integer size, String userName, Integer sellerAuditStatus);

    /** 按用户名/昵称搜索用户（用于保证金等场景的选择器），返回 id、userName、nickName */
    List<Map<String, Object>> searchUsersForSelection(String keyword, int limit);

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

    /**
     * 前台门户登录：仅允许买方/卖方（角色 1、2），不含后台岗位角色
     */
    LoginDTO loginForPublicPortal(String userName, String password, String loginIp);

    /**
     * 后台门户登录：须具备后台岗位角色（3～10）
     */
    LoginDTO loginForAdminPortal(String userName, String password, String loginIp);

    /**
     * 公开注册：仅创建买方账号（角色固定为 1）
     */
    void registerPublicUser(String userName, String password, String phone, String nickName);

    /**
     * 小程序微信登录：按微信唯一标识自动登录；首次登录自动注册为普通用户（角色1）
     */
    LoginDTO loginOrRegisterByWechat(String wxOpenid, String phone, String nickName, String avatar, String loginIp);

    /**
     * 更新个人资料（用户自己更新）
     * @param userId 当前登录用户ID
     * @param user 更新的用户信息（只更新允许的字段：nickName, realName, phone, email, sex）
     * @return 更新后的用户信息
     */
    AuctionUser updateProfile(Long userId, AuctionUser user);

    /**
     * 修改密码
     * @param userId 当前登录用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 上传并更新头像
     * @param userId 当前登录用户ID
     * @param avatarFileId 头像文件ID（关联auction_file.id）
     * @return 更新后的用户信息
     */
    AuctionUser updateAvatar(Long userId, Long avatarFileId);

    /**
     * 风控：批量更新用户风险等级（仅已存在的未删除用户）
     */
    void batchUpdateRiskLevelForRisk(List<Long> userIds, Integer riskLevel);

    /**
     * 风控：有风险行为的用户列表（用于勾选标记）
     */
    List<Map<String, Object>> listUsersWithRiskActivityForRisk();
}
