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
 * 竞拍记录表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_record")
public class AuctionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 竞拍记录ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 出价金额
     */
    @TableField("bid_price")
    private BigDecimal bidPrice;

    /**
     * 是否代理出价：0=手动出价 1=代理出价
     */
    @TableField("is_agent")
    private Integer isAgent;

    /**
     * 代理出价最高心理价位（is_agent=1时必填）
     */
    @TableField("agent_max_price")
    private BigDecimal agentMaxPrice;

    /**
     * 出价时间
     */
    @TableField("bid_time")
    private LocalDateTime bidTime;

    /**
     * 是否当前最高价：0=否 1=是（实时更新）
     */
    @TableField("is_highest")
    private Integer isHighest;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableField("del_flag")
    private Integer delFlag;


}
