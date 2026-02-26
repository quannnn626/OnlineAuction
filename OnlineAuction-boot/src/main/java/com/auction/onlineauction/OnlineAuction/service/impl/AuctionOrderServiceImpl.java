package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
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
import java.util.List;

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
}
