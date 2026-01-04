package com.auction.onlineauction.OnlineAuction.entity;

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
 * 首页轮播图表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_banner")
public class AuctionBanner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 轮播图本地路径（如D:/auction/upload/banner/202512/xxx.jpg）
     */
    @TableField("banner_img")
    private String bannerImg;

    /**
     * 关联商品ID（点击跳转，无则为NULL）
     */
    @TableField("goods_id")
    private Long goodsId;

    /**
     * 排序值（数字越小越靠前）
     */
    @TableField("banner_sort")
    private Integer bannerSort;

    /**
     * 状态：0=未启用 1=已启用
     */
    @TableField("banner_status")
    private Integer bannerStatus;

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
