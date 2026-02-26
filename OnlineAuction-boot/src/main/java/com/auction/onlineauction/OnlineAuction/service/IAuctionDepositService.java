package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 保证金记录表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionDepositService extends IService<AuctionDeposit> {

    /** 分页查询保证金记录（后台） */
    PageInfo<AuctionDeposit> getDepositPage(Integer current, Integer size, Long userId, Integer depositType);

    /** 获取用户当前保证金余额（取最新一条记录的 balance） */
    BigDecimal getBalanceByUserId(Long userId);

    /** 手动充值（财务操作，deposit_type=0） */
    void manualTopUp(Long userId, BigDecimal amount, String remark);
}
