package com.auction.onlineauction.OnlineAuction.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 拍卖商品表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_goods")
public class AuctionGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    @TableField("goods_name")
    private String goodsName;

    /**
     * 商品类目ID（关联auction_category.id）
     */
    @TableField("category_id")
    private String categoryId;

    /**
     * 卖方用户ID（关联auction_user.id）
     */
    @TableField("seller_id")
    private Long sellerId;

    /**
     * 商品详情（含瑕疵说明）
     */
    @TableField("goods_desc")
    private String goodsDesc;

    /**
     * 文件列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<AuctionFile> files;

    /**
     * 起拍价
     */
    @TableField("base_price")
    private BigDecimal basePrice;

    /**
     * 最小加价幅度
     */
    @TableField("add_price")
    private BigDecimal addPrice;

    /**
     * 保留价（仅卖方可见）
     */
    @TableField("reserve_price")
    private BigDecimal reservePrice;

    /**
     * 拍卖开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 拍卖结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 审核状态：0=待审核 1=审核通过 2=审核驳回 3=已下架
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核驳回原因
     */
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 商品状态：0=未开始 1=竞拍中 2=已成交 3=已流拍
     */
    @TableField("goods_status")
    private Integer goodsStatus;

    /**
     * 创建时间
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
