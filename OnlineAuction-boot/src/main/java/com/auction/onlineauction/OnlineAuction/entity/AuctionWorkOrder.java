package com.auction.onlineauction.OnlineAuction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客服工单表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_work_order")
public class AuctionWorkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("work_no")
    private String workNo;

    @TableField("user_id")
    private Long userId;

    @TableField("service_id")
    private Long serviceId;

    @TableField("work_type")
    private String workType;

    @TableField("related_id")
    private Long relatedId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("work_status")
    private Integer workStatus;

    @TableField("handle_result")
    private String handleResult;

    @TableField("review_result")
    private String reviewResult;

    @TableField("review_admin_id")
    private Long reviewAdminId;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("penalty_type")
    private String penaltyType;

    @TableField("penalty_target_user_id")
    private Long penaltyTargetUserId;

    @TableField("penalty_amount")
    private BigDecimal penaltyAmount;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
