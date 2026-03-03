package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;

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

    /** 客服查看：仅展示咨询过该客服的用户订单，按创建时间倒序 */
    PageInfo<AuctionOrder> getOrderPageForConsultedUsers(Long serviceId, Integer current, Integer size,
                                                        Integer orderStatus, String orderNo);

    /** 客服是否有权查看该订单（订单买方或卖方曾向该客服咨询过） */
    boolean canServiceViewOrder(Long orderId, Long serviceId);

    /** 更新订单状态（结算：待付款->待发货；确认收货：待收货->已完成） */
    void updateOrderStatus(Long orderId, Integer newStatus);

    /** 处理退款：订单状态改为已退款，解冻买方保证金 */
    void processRefund(Long orderId, String remark);

    /** 指定商品是否已存在有效订单 */
    boolean existsActiveOrderByGoodsId(Long goodsId);

    /** 根据中标记录创建订单 */
    AuctionOrder createWinningOrder(Long goodsId, Long sellerId, AuctionRecord highestRecord, LocalDateTime payDeadline);
}
