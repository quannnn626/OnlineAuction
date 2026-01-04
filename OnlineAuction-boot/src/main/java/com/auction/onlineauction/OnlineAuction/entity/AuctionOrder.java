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
 * 竞拍订单表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_order")
public class AuctionOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号（唯一索引，规则：YYYYMMDD+随机8位）
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 商品ID（关联auction_goods.id）
     */
    @TableField("goods_id")
    private Long goodsId;

    /**
     * 买方用户ID（关联auction_user.id）
     */
    @TableField("buyer_id")
    private Long buyerId;

    /**
     * 卖方用户ID（关联auction_user.id）
     */
    @TableField("seller_id")
    private Long sellerId;

    /**
     * 成交竞拍记录ID（关联auction_record.id）
     */
    @TableField("record_id")
    private Long recordId;

    /**
     * 成交金额
     */
    @TableField("deal_price")
    private BigDecimal dealPrice;

    /**
     * 冻结保证金金额
     */
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    /**
     * 尾款金额（deal_price - deposit_amount）
     */
    @TableField("remain_amount")
    private BigDecimal remainAmount;

    /**
     * 尾款付款截止时间（成交后24小时）
     */
    @TableField("pay_deadline")
    private LocalDateTime payDeadline;

    /**
     * 订单状态：0=待付款 1=待发货 2=待收货 3=已完成 4=已悔拍 5=已退款
     */
    @TableField("order_status")
    private Integer orderStatus;

    /**
     * 创建时间（竞拍成交后系统自动生成）
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableField("del_flag")
    private Integer delFlag;


}
