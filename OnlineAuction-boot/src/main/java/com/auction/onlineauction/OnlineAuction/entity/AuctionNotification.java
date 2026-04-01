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
 * 管理沟通通知主表
 * 表：auction_notification
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_notification")
public class AuctionNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("sender_id")
    private Long senderId;

    @TableField("notice_title")
    private String noticeTitle;

    @TableField("notice_content")
    private String noticeContent;

    /**
     * 通知类型：1-5
     */
    @TableField("notice_type")
    private Integer noticeType;

    /**
     * 是否需要确认收到：0=否 1=是
     */
    @TableField("need_confirm")
    private Integer needConfirm;

    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableField("del_flag")
    private Integer delFlag;
}

