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
 * 平台扣费/佣金记录表（财务查看）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_fee_record")
public class AuctionFeeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    @TableField("amount")
    private BigDecimal amount;

    /** 1=佣金 2=违约金 3=违规扣除保证金 4=其他手续费 */
    @TableField("fee_type")
    private Integer feeType;

    /** 0=待结算 1=已结算 */
    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("settle_time")
    private LocalDateTime settleTime;

    @TableField("del_flag")
    private Integer delFlag;
}
