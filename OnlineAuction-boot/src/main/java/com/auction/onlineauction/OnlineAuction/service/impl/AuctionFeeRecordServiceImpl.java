package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFeeRecord;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionFeeRecordMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFeeRecordService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuctionFeeRecordServiceImpl extends ServiceImpl<AuctionFeeRecordMapper, AuctionFeeRecord> implements IAuctionFeeRecordService {
    /**
     * 使用Lazy注解
     * 这样在创建 AuctionFeeRecordServiceImpl 时只会注入一个懒加载代理，
     * 不会马上去创建 AuctionOrderServiceImpl，从而打破循环。
     */
    @Autowired
    @Lazy
    private IAuctionOrderService orderService;

    @Override
    public PageInfo<AuctionFeeRecord> getPage(Integer current, Integer size, Integer feeType, Integer status, Long userId) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionFeeRecord> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (feeType != null) q.eq("fee_type", feeType);
        if (status != null) q.eq("status", status);
        if (userId != null) q.eq("user_id", userId);
        q.orderByDesc("create_time");
        List<AuctionFeeRecord> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDeduction(Long userId, Long orderId, BigDecimal amount, int feeType, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return;
        AuctionFeeRecord r = new AuctionFeeRecord();
        r.setUserId(userId);
        r.setOrderId(orderId);
        r.setAmount(amount);
        r.setFeeType(feeType);
        r.setStatus(1);
        r.setRemark(remark != null ? remark : "");
        r.setCreateTime(LocalDateTime.now());
        r.setSettleTime(LocalDateTime.now());
        r.setDelFlag(0);
        save(r);
    }

    @Override
    public List<Long> getOrderIdsWithCommissionSettled() {
        return baseMapper.selectOrderIdsWithCommissionSettled();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmCommissionSettle(Long orderId, BigDecimal commissionRate, BigDecimal overrideAmount) {
        AuctionOrder order = orderService.getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 3) {
            throw new RuntimeException("仅已完成的订单可做佣金结算");
        }
        if (order.getSellerId() == null) {
            throw new RuntimeException("订单无卖方信息");
        }
        QueryWrapper<AuctionFeeRecord> q = new QueryWrapper<>();
        q.eq("order_id", orderId).eq("fee_type", 1).eq("del_flag", 0);
        if (count(q) > 0) {
            throw new RuntimeException("该订单已做过佣金结算");
        }
        BigDecimal amount;
        if (overrideAmount != null && overrideAmount.compareTo(BigDecimal.ZERO) >= 0) {
            amount = overrideAmount.setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal rate = commissionRate != null && commissionRate.compareTo(BigDecimal.ZERO) >= 0
                ? commissionRate : BigDecimal.ZERO;
            BigDecimal dealPrice = order.getDealPrice() != null ? order.getDealPrice() : BigDecimal.ZERO;
            amount = dealPrice.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;
        String orderNo = order.getOrderNo() != null ? order.getOrderNo() : ("订单" + orderId);
        String remark = "订单佣金结算，订单号：" + orderNo + "，成交价×佣金比例";
        recordDeduction(order.getSellerId(), orderId, amount, 1, remark);
    }

    @Override
    public List<AuctionFeeRecord> listForExport(Integer feeType, Integer status, Long userId, String startTime, String endTime) {
        QueryWrapper<AuctionFeeRecord> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (feeType != null) q.eq("fee_type", feeType);
        if (status != null) q.eq("status", status);
        if (userId != null) q.eq("user_id", userId);
        if (startTime != null && !startTime.trim().isEmpty()) {
            try {
                String s = startTime.trim();
                LocalDateTime st = s.length() > 10
                    ? LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : LocalDateTime.parse(s + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                q.ge("create_time", st);
            } catch (Exception ignored) {}
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            try {
                String s = endTime.trim();
                LocalDateTime et = s.length() > 10
                    ? LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : LocalDateTime.parse(s + "T23:59:59", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                q.le("create_time", et);
            } catch (Exception ignored) {}
        }
        q.orderByDesc("create_time");
        return list(q);
    }
}
