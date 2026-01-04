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
 * 留言板表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_message")
public class AuctionMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 留言ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 留言用户ID（关联auction_user.id）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关联商品ID（无则为NULL）
     */
    @TableField("goods_id")
    private Long goodsId;

    /**
     * 留言内容
     */
    @TableField("message_content")
    private String messageContent;

    /**
     * 管理员回复内容
     */
    @TableField("reply_content")
    private String replyContent;

    /**
     * 回复时间
     */
    @TableField("reply_time")
    private LocalDateTime replyTime;

    /**
     * 状态：0=未回复 1=已回复
     */
    @TableField("message_status")
    private Integer messageStatus;

    /**
     * 留言时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableField("del_flag")
    private Integer delFlag;


}
