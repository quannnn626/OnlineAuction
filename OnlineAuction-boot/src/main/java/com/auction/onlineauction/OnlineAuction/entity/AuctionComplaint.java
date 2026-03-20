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
 * 用户投诉表（商品/订单/竞价/留言）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_complaint")
public class AuctionComplaint implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("complaint_user_id")
    private Long complaintUserId;

    @TableField("complaint_type")
    private String complaintType;

    @TableField("target_id")
    private Long targetId;

    @TableField("related_goods_id")
    private Long relatedGoodsId;

    @TableField("related_order_id")
    private Long relatedOrderId;

    @TableField("related_message_id")
    private Long relatedMessageId;

    @TableField("reported_user_id")
    private Long reportedUserId;

    @TableField("complaint_content")
    private String complaintContent;

    @TableField("evidence")
    private String evidence;

    @TableField("complaint_status")
    private Integer complaintStatus;

    @TableField("handle_result")
    private String handleResult;

    @TableField("handle_user_id")
    private Long handleUserId;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
