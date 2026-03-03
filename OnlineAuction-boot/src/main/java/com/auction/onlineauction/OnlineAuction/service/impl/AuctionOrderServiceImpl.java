package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageSession;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMessageSessionMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionOrderMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * 竞拍订单表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionOrderServiceImpl extends ServiceImpl<AuctionOrderMapper, AuctionOrder> implements IAuctionOrderService {

    @Autowired
    private IAuctionDepositService depositService;
    @Autowired
    private AuctionMessageSessionMapper messageSessionMapper;

    @Override
    public PageInfo<AuctionOrder> getOrderPage(Integer current, Integer size, Integer orderStatus,
                                               Long buyerId, Long sellerId, String orderNo) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionOrder> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (orderStatus != null) q.eq("order_status", orderStatus);
        if (buyerId != null) q.eq("buyer_id", buyerId);
        if (sellerId != null) q.eq("seller_id", sellerId);
        if (orderNo != null && !orderNo.trim().isEmpty()) q.like("order_no", orderNo.trim());
        q.orderByDesc("create_time");
        List<AuctionOrder> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AuctionOrder> getOrderPageForConsultedUsers(Long serviceId, Integer current, Integer size,
                                                               Integer orderStatus, String orderNo) {
        if (serviceId == null) {
            return new PageInfo<>(Collections.emptyList());
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AuctionMessageSession> sw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        sw.eq("service_id", serviceId).eq("del_flag", 0).select("user_id");
        List<AuctionMessageSession> sessions = messageSessionMapper.selectList(sw);
        Set<Long> userIds = sessions.stream().map(AuctionMessageSession::getUserId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionOrder> q = new QueryWrapper<>();
        q.eq("del_flag", 0).and(w -> w.in("buyer_id", userIds).or().in("seller_id", userIds));
        if (orderStatus != null) q.eq("order_status", orderStatus);
        if (orderNo != null && !orderNo.trim().isEmpty()) q.like("order_no", orderNo.trim());
        q.orderByDesc("create_time");
        List<AuctionOrder> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    public boolean canServiceViewOrder(Long orderId, Long serviceId) {
        if (orderId == null || serviceId == null) return false;
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) return false;
        java.util.Set<Long> partyIds = new java.util.HashSet<>();
        if (order.getBuyerId() != null) partyIds.add(order.getBuyerId());
        if (order.getSellerId() != null) partyIds.add(order.getSellerId());
        if (partyIds.isEmpty()) return false;
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AuctionMessageSession> sw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        sw.eq("service_id", serviceId).eq("del_flag", 0).in("user_id", partyIds);
        return messageSessionMapper.selectCount(sw) > 0;
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer newStatus) {
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        Integer old = order.getOrderStatus();
        if (old.equals(newStatus)) return;
        if (newStatus == 1 && old != 0) {
            throw new RuntimeException("只有待付款订单可标记为待发货");
        }
        if (newStatus == 3 && old != 2) {
            throw new RuntimeException("只有待收货订单可标记为已完成");
        }
        order.setOrderStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long orderId, String remark) {
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() == 5) {
            throw new RuntimeException("订单已退款");
        }
        if (order.getOrderStatus() == 4) {
            throw new RuntimeException("悔拍订单不可退款");
        }
        BigDecimal toRefund = order.getDepositAmount();
        if (toRefund != null && toRefund.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balance = depositService.getBalanceByUserId(order.getBuyerId());
            BigDecimal newBalance = balance.add(toRefund);
            AuctionDeposit record = new AuctionDeposit();
            record.setUserId(order.getBuyerId());
            record.setOrderId(orderId);
            record.setAmount(toRefund);
            record.setDepositType(2);
            record.setBalance(newBalance);
            record.setOperateTime(LocalDateTime.now());
            record.setRemark(remark != null ? remark : "订单退款，解冻保证金");
            record.setDelFlag(0);
            depositService.save(record);
        }
        order.setOrderStatus(5);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public boolean existsActiveOrderByGoodsId(Long goodsId) {
        if (goodsId == null) {
            return false;
        }
        QueryWrapper<AuctionOrder> q = new QueryWrapper<>();
        q.eq("goods_id", goodsId).eq("del_flag", 0);
        return count(q) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionOrder createWinningOrder(Long goodsId, Long sellerId, AuctionRecord highestRecord, LocalDateTime payDeadline) {
        if (goodsId == null || sellerId == null || highestRecord == null) {
            throw new RuntimeException("创建订单参数不完整");
        }
        if (existsActiveOrderByGoodsId(goodsId)) {
            throw new RuntimeException("该商品已存在订单");
        }

        BigDecimal dealPrice = highestRecord.getBidPrice();
        if (dealPrice == null || dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("中标价格无效");
        }

        BigDecimal depositAmount = BigDecimal.ZERO;
        BigDecimal remainAmount = dealPrice.subtract(depositAmount);

        AuctionOrder order = new AuctionOrder();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goodsId);
        order.setBuyerId(highestRecord.getBuyerId());
        order.setSellerId(sellerId);
        order.setRecordId(highestRecord.getId());
        order.setDealPrice(dealPrice);
        order.setDepositAmount(depositAmount);
        order.setRemainAmount(remainAmount);
        order.setPayDeadline(payDeadline != null ? payDeadline : LocalDateTime.now().plusHours(24));
        order.setOrderStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDelFlag(0);

        boolean saved = save(order);
        if (!saved) {
            throw new RuntimeException("创建中标订单失败");
        }
        return order;
    }

    private String generateOrderNo() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int suffix = ThreadLocalRandom.current().nextInt(10000000, 100000000);
        return prefix + suffix;
    }
}
