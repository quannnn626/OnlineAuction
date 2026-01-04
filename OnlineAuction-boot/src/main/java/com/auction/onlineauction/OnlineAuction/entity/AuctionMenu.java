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
 * 系统菜单表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_menu")
public class AuctionMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 父菜单ID（0表示顶级菜单）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 菜单路径/路由（如：/goods/list）
     */
    @TableField("menu_path")
    private String menuPath;

    /**
     * 菜单图标（Element UI图标类名，如：el-icon-goods）
     */
    @TableField("menu_icon")
    private String menuIcon;

    /**
     * 菜单类型：1=菜单 2=按钮
     */
    @TableField("menu_type")
    private Integer menuType;

    /**
     * 权限标识（如：goods:list，用于权限控制）
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 排序值（数字越小越靠前）
     */
    @TableField("menu_sort")
    private Integer menuSort;

    /**
     * 状态：0=禁用 1=启用
     */
    @TableField("menu_status")
    private Integer menuStatus;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;

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

    /**
     * 子菜单列表（非数据库字段，用于构建菜单树）
     */
    @TableField(exist = false)
    private java.util.List<AuctionMenu> children;

}
