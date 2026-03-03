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
 * 消息中心消息表
 * content_type: 1=文本 2=订单信息 3=附件
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_message_center")
public class AuctionMessageCenter implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("sender_id")
    private Long senderId;

    @TableField("receiver_id")
    private Long receiverId;

    /** 1=文本 2=订单信息 3=附件 */
    @TableField("content_type")
    private Integer contentType;

    @TableField("content")
    private String content;

    @TableField("file_id")
    private Long fileId;

    @TableField("is_read")
    private Integer isRead;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("del_flag")
    private Integer delFlag;
}
