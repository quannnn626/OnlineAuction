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
 * 系统操作日志表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_oper_log")
public class AuctionOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID（关联auction_user.id）
     */
    @TableField("oper_user_id")
    private Long operUserId;

    /**
     * 操作模块（如：商品审核、用户管理）
     */
    @TableField("oper_module")
    private String operModule;

    /**
     * 操作类型：新增/修改/删除/审核/禁用
     */
    @TableField("oper_type")
    private String operType;

    /**
     * 操作内容（如：审核商品ID=123，状态改为通过）
     */
    @TableField("oper_content")
    private String operContent;

    /**
     * 操作IP地址
     */
    @TableField("oper_ip")
    private String operIp;

    /**
     * 操作时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;


}
