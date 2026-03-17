package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /** 平台保证金汇总（总额、可用、冻结、用户数） */
    Map<String, Object> getPlatformSummary();

    /** 用户保证金汇总分页（按用户名/昵称搜索） */
    PageInfo<Map<String, Object>> getDepositSummaryPage(Integer current, Integer size, String userName);

    /** 手动充值（财务操作，deposit_type=0） */
    void manualTopUp(Long userId, BigDecimal amount, String remark);

    /** 参与竞拍时冻结保证金（deposit_type=1，order_id 为空，remark 含商品ID） */
    void freezeForBid(Long userId, BigDecimal amount, Long goodsId);

    /** 悔拍扣除保证金（deposit_type=4，不退还） */
    void deductForDefault(Long userId, BigDecimal amount, Long orderId, String remark);

    /** 财务冻结用户保证金（deposit_type=5） */
    void freezeByFinance(Long userId, BigDecimal amount, String remark);

    /** 财务解冻用户保证金（deposit_type=6） */
    void unfreezeByFinance(Long userId, BigDecimal amount, String remark);
}
