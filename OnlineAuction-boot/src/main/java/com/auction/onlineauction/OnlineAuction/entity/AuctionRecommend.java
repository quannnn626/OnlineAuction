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
 * 推荐位表（首页推荐、分类推荐）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_recommend")
public class AuctionRecommend implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_HOME = "home";
    public static final String TYPE_CATEGORY = "category";

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("recommend_type")
    private String recommendType;

    @TableField("target_id")
    private Long targetId;

    @TableField("goods_id")
    private Long goodsId;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
