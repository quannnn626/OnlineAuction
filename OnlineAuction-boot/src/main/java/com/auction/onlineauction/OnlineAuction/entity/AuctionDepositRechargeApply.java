package com.auction.onlineauction.OnlineAuction.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 保证金充值申请表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_deposit_recharge_apply")
public class AuctionDepositRechargeApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("amount")
    private BigDecimal amount;

    /** 0=待审核 1=已通过 2=已驳回 */
    @TableField("status")
    private Integer status;

    @TableField("apply_remark")
    private String applyRemark;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("handle_user_id")
    private Long handleUserId;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("del_flag")
    private Integer delFlag;
}
