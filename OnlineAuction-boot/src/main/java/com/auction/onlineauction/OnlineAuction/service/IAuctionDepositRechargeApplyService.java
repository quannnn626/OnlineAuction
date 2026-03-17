package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDepositRechargeApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;

/**
 * 保证金充值申请（用户申请，财务审核后充值）
 */
public interface IAuctionDepositRechargeApplyService extends IService<AuctionDepositRechargeApply> {

    PageInfo<AuctionDepositRechargeApply> getMyPage(Integer current, Integer size, Long userId);

    PageInfo<AuctionDepositRechargeApply> getAdminPage(Integer current, Integer size, Integer status);

    Long apply(Long userId, BigDecimal amount, String applyRemark);

    void approve(Long id, Long handleUserId, String handleRemark);

    void reject(Long id, Long handleUserId, String handleRemark);
}
