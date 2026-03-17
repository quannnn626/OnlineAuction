package com.auction.onlineauction.OnlineAuction.service;

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

    Long apply(Long userId, Long orderId, String invoiceTitle, String taxNo, BigDecimal amount, Integer invoiceType);

    void handleInvoice(Long id, Integer status, Long fileId, String handleRemark, Long handleUserId);
}
