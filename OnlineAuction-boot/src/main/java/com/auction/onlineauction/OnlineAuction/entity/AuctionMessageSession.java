package com.auction.onlineauction.OnlineAuction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息中心会话表（以商品为核心，商品-用户唯一）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_message_session")
public class AuctionMessageSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("goods_id")
    private Long goodsId;

    @TableField("user_id")
    private Long userId;

    @TableField("service_id")
    private Long serviceId;

    /** 0=进行中 1=已关闭 */
    @TableField("session_status")
    private Integer sessionStatus;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
