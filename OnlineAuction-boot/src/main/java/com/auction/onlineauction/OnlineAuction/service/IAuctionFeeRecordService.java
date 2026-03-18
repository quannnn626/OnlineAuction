package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFeeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 平台扣费/佣金记录（财务查看）
 */
public interface IAuctionFeeRecordService extends IService<AuctionFeeRecord> {

    PageInfo<AuctionFeeRecord> getPage(Integer current, Integer size, Integer feeType, Integer status, Long userId);

    /** 记录一笔平台扣费（佣金、违约金、违规扣保证金、其他） */
    void recordDeduction(Long userId, Long orderId, BigDecimal amount, int feeType, String remark);

    /** 已结算佣金的订单ID列表（用于筛选待结算订单） */
    List<Long> getOrderIdsWithCommissionSettled();

    /** 对指定已完成订单做佣金结算确认：计算佣金并写入扣费记录（status=已结算） */
    void confirmCommissionSettle(Long orderId, BigDecimal commissionRate, BigDecimal overrideAmount);

    /** 导出用：扣费记录列表（支持类型、状态、用户、时间范围） */
    List<AuctionFeeRecord> listForExport(Integer feeType, Integer status, Long userId, String startTime, String endTime);
}
