package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.entity.AuctionInvoice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;

/**
 * 发票申请（用户申请，财务处理/上传）
 */
public interface IAuctionInvoiceService extends IService<AuctionInvoice> {

    PageInfo<AuctionInvoice> getMyPage(Integer current, Integer size, Long userId);

    PageInfo<AuctionInvoice> getAdminPage(Integer current, Integer size, Integer status, Long userId);

    Long apply(Long userId, Long orderId, String invoiceTitle, String taxNo, String mailAddress, BigDecimal amount, Integer invoiceType);

    void handleInvoice(Long id, Integer status, Long fileId, String handleRemark, Long handleUserId);

    /** 用户获取本人某条发票申请对应的发票文件信息（用于下载/查看），仅已开票且有文件时返回 */
    AuctionFile getMyInvoiceFile(Long invoiceId, Long userId);
}
