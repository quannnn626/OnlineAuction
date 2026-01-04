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
 * 竞拍公告表
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_notice")
public class AuctionNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    @TableField("notice_title")
    private String noticeTitle;

    /**
     * 公告内容
     */
    @TableField("notice_content")
    private String noticeContent;

    /**
     * 是否置顶：0=否 1=是
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 状态：0=未发布 1=已发布 2=已下架
     */
    @TableField("notice_status")
    private Integer noticeStatus;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private LocalDateTime publishTime;

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
