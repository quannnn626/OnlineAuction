package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

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

    /** 分页查询订单并填充商品名、买方名、卖方名（后台展示用） */
    PageInfo<Map<String, Object>> getOrderPageWithDisplayNames(Integer current, Integer size, Integer orderStatus,
                                                               Long buyerId, Long sellerId, String orderNo);

    /** 客服查看指定用户的订单（仅当该用户与客服存在会话时），返回带名称的列表 */
    PageInfo<Map<String, Object>> getOrderPageForUserByService(Long serviceId, Long targetUserId, Integer current,
                                                               Integer size, Integer orderStatus, String orderNo);

    /** 客服查看：仅展示咨询过该客服的用户订单，按创建时间倒序 */
    PageInfo<AuctionOrder> getOrderPageForConsultedUsers(Long serviceId, Integer current, Integer size,
                                                        Integer orderStatus, String orderNo);

    /** 客服是否有权查看该订单（订单买方或卖方曾向该客服咨询过） */
    boolean canServiceViewOrder(Long orderId, Long serviceId);

    /** 更新订单状态（结算：待付款->待发货；确认收货：待收货->已完成） */
    void updateOrderStatus(Long orderId, Integer newStatus);

    /** 处理退款：订单状态改为已退款，解冻买方保证金 */
    void processRefund(Long orderId, String remark);

    /** 落槌确认：拍卖师/管理员确认成交，生成成交确认书编号，锁定买受人 */
    void confirmDeal(Long orderId, Long confirmUserId);

    /** 发货：填写快递信息，订单状态 待发货->待收货 */
    void shipOrder(Long orderId, String expressCompany, String expressNo);

    /** 买方确认收货：仅买方本人可操作，待收货->已完成 */
    void confirmReceiptByBuyer(Long orderId, Long buyerId);

    /** 买方支付尾款：待付款订单，用保证金抵扣尾款后订单变为待发货 */
    void payByBuyer(Long orderId, Long buyerId);

    /** 指定商品是否已存在有效订单 */
    boolean existsActiveOrderByGoodsId(Long goodsId);

    /** 根据中标记录创建订单（depositAmount 为商品设定的保证金，可为 0） */
    AuctionOrder createWinningOrder(Long goodsId, Long sellerId, AuctionRecord highestRecord, LocalDateTime payDeadline, BigDecimal depositAmount);

    /** 悔拍：扣除买方保证金，订单置为已悔拍，商品恢复为上架状态供拍卖师再次上架 */
    void markOrderAsDefaulted(Long orderId);

    /** 已完成且未做佣金结算的订单分页（带商品名、买卖方名，供财务结算） */
    PageInfo<Map<String, Object>> getOrdersPendingCommissionSettlement(Integer current, Integer size);
}
