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
 * 拍卖商品类目表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_category")
public class AuctionCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类目ID，自增
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 类目名称（如：奢侈品、房产、艺术品）
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 排序值（数字越小越靠前）
     */
    @TableField("category_sort")
    private Integer categorySort;

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
