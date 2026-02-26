package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

/**
 * <p>
 * 竞拍订单表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionOrderService extends IService<AuctionOrder> {

    /** 分页查询订单（后台，支持状态筛选） */
    PageInfo<AuctionOrder> getOrderPage(Integer current, Integer size, Integer orderStatus,
                                        Long buyerId, Long sellerId, String orderNo);

    /** 更新订单状态（结算：待付款->待发货；确认收货：待收货->已完成） */
    void updateOrderStatus(Long orderId, Integer newStatus);

    /** 处理退款：订单状态改为已退款，解冻买方保证金 */
    void processRefund(Long orderId, String remark);
}
