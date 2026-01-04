package com.auction.onlineauction.OnlineAuction.entity;

import java.math.BigDecimal;
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
 * 保证金记录表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_deposit")
public class AuctionDeposit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 保证金记录ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联auction_user.id）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关联订单ID（auction_order.id，无订单时为NULL）
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 保证金金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 变动类型：0=充值 1=冻结 2=解冻 3=抵扣尾款 4=扣除（悔拍）
     */
    @TableField("deposit_type")
    private Integer depositType;

    /**
     * 操作后保证金余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private LocalDateTime operateTime;

    /**
     * 备注（如：冻结订单ID=123保证金）
     */
    @TableField("remark")
    private String remark;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableField("del_flag")
    private Integer delFlag;


}
