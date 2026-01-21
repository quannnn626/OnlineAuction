package com.auction.onlineauction.OnlineAuction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统用户表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_user")
public class AuctionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    @TableField("user_name")
    private String userName;

    /**
     * 密码（MD5加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名（实名认证用）
     */
    @TableField("real_name")
    private String realName;

    /**
     * 用户昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 手机号（唯一索引）
     */
    @TableField("phone")
    private String phone;

    /**
     * 用户邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 用户头像地址
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @TableField("sex")
    private String sex;

    /**
     * 用户角色（多个角色用逗号分隔）：1=买方用户 2=卖方用户 3=后台管理员
     * 例如："1" 表示只有买方身份，"1,2" 表示同时拥有买方和卖方身份
     */
    @TableField("user_role")
    private String userRole;

    /**
     * 卖方资质审核状态：0=未提交 1=待审核 2=审核通过 3=审核驳回（买方默认为0）
     */
    @TableField("seller_audit_status")
    private Integer sellerAuditStatus;

    /**
     * 卖方资质审核驳回原因
     */
    @TableField("seller_audit_remark")
    private String sellerAuditRemark;

    /**
     * 卖方资质证明材料（文件路径，多个文件用逗号分隔）
     */
    @TableField("seller_certificate_files")
    private String sellerCertificateFiles;

    /**
     * 卖方资质申请时间
     */
    @TableField("seller_audit_apply_time")
    private LocalDateTime sellerAuditApplyTime;

    /**
     * 卖方资质审核时间
     */
    @TableField("seller_audit_time")
    private LocalDateTime sellerAuditTime;

    /**
     * 卖方资质审核人ID（管理员ID）
     */
    @TableField("seller_audit_user_id")
    private Long sellerAuditUserId;

    /**
     * 账号状态：0=正常 1=禁用（替代物理删除）
     */
    @TableField("user_status")
    private Integer userStatus;

    /**
     * 最后登录IP
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @TableField("login_date")
    private LocalDateTime loginDate;

    /**
     * 创建时间（自动填充）
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableField("del_flag")
    private Integer delFlag;


}
