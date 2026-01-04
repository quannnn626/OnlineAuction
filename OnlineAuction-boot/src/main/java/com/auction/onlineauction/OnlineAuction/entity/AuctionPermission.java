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
 * 系统权限表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_permission")
public class AuctionPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限标识（唯一，如：goods:list, goods:add）
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限类型：1=菜单权限 2=按钮权限 3=数据权限
     */
    @TableField("permission_type")
    private Integer permissionType;

    /**
     * 权限描述
     */
    @TableField("permission_desc")
    private String permissionDesc;

    /**
     * 状态：0=禁用 1=启用
     */
    @TableField("permission_status")
    private Integer permissionStatus;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableField("del_flag")
    private Integer delFlag;


}
