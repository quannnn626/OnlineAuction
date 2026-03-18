package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 竞拍订单表 Mapper 接口
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface AuctionOrderMapper extends BaseMapper<AuctionOrder> {

    /** 已完成且未做佣金结算的订单（order_status=3 且 无 fee_type=1 记录） */
    List<AuctionOrder> selectOrdersPendingCommissionSettlement();
}
