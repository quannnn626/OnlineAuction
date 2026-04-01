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
 * 管理沟通通知接收状态表
 * 表：auction_notification_target
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_notification_target")
public class AuctionNotificationTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("notification_id")
    private Long notificationId;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("is_read")
    private Integer isRead;

    @TableField("is_confirmed")
    private Integer isConfirmed;

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    @TableField("del_flag")
    private Integer delFlag;
}

