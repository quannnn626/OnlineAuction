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
 * 发票申请表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auction_invoice")
public class AuctionInvoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("order_id")
    private Long orderId;

    @TableField("invoice_title")
    private String invoiceTitle;

    @TableField("tax_no")
    private String taxNo;

    @TableField("mail_address")
    private String mailAddress;

    @TableField("amount")
    private BigDecimal amount;

    /** 1=普票 2=专票 */
    @TableField("invoice_type")
    private Integer invoiceType;

    /** 0=待处理 1=已开票 2=已驳回 */
    @TableField("status")
    private Integer status;

    @TableField("file_id")
    private Long fileId;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("handle_user_id")
    private Long handleUserId;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("del_flag")
    private Integer delFlag;
}
