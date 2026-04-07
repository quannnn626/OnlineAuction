package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageSession;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMessageSessionMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionOrderMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFeeRecordService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private IAuctionFeeRecordService feeRecordService;
    @Autowired
    private AuctionMessageSessionMapper messageSessionMapper;
    @Autowired
    @Lazy
    private IAuctionGoodsService goodsService;
    @Autowired
    private IAuctionUserService userService;

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
    public PageInfo<Map<String, Object>> getOrderPageWithDisplayNames(Integer current, Integer size, Integer orderStatus,
                                                                     Long buyerId, Long sellerId, String orderNo) {
        PageInfo<AuctionOrder> raw = getOrderPage(current, size, orderStatus, buyerId, sellerId, orderNo);
        List<Map<String, Object>> rows = buildOrderRowsWithNames(raw.getList());
        PageInfo<Map<String, Object>> result = new PageInfo<>(rows);
        result.setTotal(raw.getTotal());
        result.setPages(raw.getPages());
        result.setPageNum(raw.getPageNum());
        result.setPageSize(raw.getPageSize());
        return result;
    }

    @Override
    public PageInfo<Map<String, Object>> getOrderPageForUserByService(Long serviceId, Long targetUserId, Integer current,
                                                                       Integer size, Integer orderStatus, String orderNo) {
        if (serviceId == null || targetUserId == null) {
            return new PageInfo<>(new ArrayList<>());
        }
        QueryWrapper<AuctionMessageSession> sw = new QueryWrapper<>();
        sw.eq("service_id", serviceId).eq("user_id", targetUserId).eq("del_flag", 0);
        if (messageSessionMapper.selectCount(sw) == 0) {
            return new PageInfo<>(new ArrayList<>());
        }
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionOrder> q = new QueryWrapper<>();
        q.eq("del_flag", 0).and(w -> w.eq("buyer_id", targetUserId).or().eq("seller_id", targetUserId));
        if (orderStatus != null) q.eq("order_status", orderStatus);
        if (orderNo != null && !orderNo.trim().isEmpty()) q.like("order_no", orderNo.trim());
        q.orderByDesc("create_time");
        List<AuctionOrder> list = list(q);
        PageInfo<AuctionOrder> raw = new PageInfo<>(list);
        List<Map<String, Object>> rows = buildOrderRowsWithNames(raw.getList());
        PageInfo<Map<String, Object>> result = new PageInfo<>(rows);
        result.setTotal(raw.getTotal());
        result.setPages(raw.getPages());
        result.setPageNum(raw.getPageNum());
        result.setPageSize(raw.getPageSize());
        return result;
    }

    private List<Map<String, Object>> buildOrderRowsWithNames(List<AuctionOrder> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        Set<Long> goodsIds = list.stream().map(AuctionOrder::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = new java.util.HashSet<>();
        list.forEach(o -> {
            if (o.getBuyerId() != null) userIds.add(o.getBuyerId());
            if (o.getSellerId() != null) userIds.add(o.getSellerId());
        });
        Map<Long, AuctionGoods> goodsMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().filter(g -> g.getDelFlag() == 0).collect(Collectors.toMap(AuctionGoods::getId, g -> g));
        Map<Long, AuctionUser> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userService.listByIds(new ArrayList<>(userIds)).stream().filter(u -> u.getDelFlag() == 0).collect(Collectors.toMap(AuctionUser::getId, u -> u));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AuctionOrder o : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", o.getId());
            row.put("orderNo", o.getOrderNo());
            row.put("goodsId", o.getGoodsId());
            row.put("buyerId", o.getBuyerId());
            row.put("sellerId", o.getSellerId());
            AuctionGoods g = o.getGoodsId() != null ? goodsMap.get(o.getGoodsId()) : null;
            row.put("goodsName", g != null ? g.getGoodsName() : "-");
            row.put("buyerName", userName(userMap.get(o.getBuyerId())));
            row.put("sellerName", userName(userMap.get(o.getSellerId())));
            row.put("dealPrice", o.getDealPrice());
            row.put("depositAmount", o.getDepositAmount());
            row.put("remainAmount", o.getRemainAmount());
            row.put("payDeadline", o.getPayDeadline());
            row.put("orderStatus", o.getOrderStatus());
            row.put("confirmDealAt", o.getConfirmDealAt());
            row.put("confirmationNo", o.getConfirmationNo());
            row.put("expressCompany", o.getExpressCompany());
            row.put("expressNo", o.getExpressNo());
            row.put("shipTime", o.getShipTime());
            row.put("createTime", o.getCreateTime());
            row.put("updateTime", o.getUpdateTime());
            rows.add(row);
        }
        return rows;
    }

    private static String userName(AuctionUser u) {
        if (u == null) return "-";
        if (u.getNickName() != null && !u.getNickName().trim().isEmpty()) return u.getNickName().trim();
        return u.getUserName() != null ? u.getUserName() : "-";
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
        q.in("order_status", 0, 1, 2, 3);
        return count(q) > 0;
    }

    @Override
    public boolean hasTerminalFailureOrderByGoodsId(Long goodsId) {
        if (goodsId == null) {
            return false;
        }
        QueryWrapper<AuctionOrder> q = new QueryWrapper<>();
        q.eq("goods_id", goodsId).eq("del_flag", 0);
        q.in("order_status", 4, 5);
        return count(q) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionOrder createWinningOrder(Long goodsId, Long sellerId, AuctionRecord highestRecord, LocalDateTime payDeadline, BigDecimal depositAmount) {
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

        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) < 0) {
            depositAmount = BigDecimal.ZERO;
        }
        if (depositAmount.compareTo(dealPrice) > 0) {
            depositAmount = dealPrice;
        }
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
        int suffix = java.util.concurrent.ThreadLocalRandom.current().nextInt(10000000, 100000000);
        return prefix + suffix;
    }

    @Override
    public void confirmDeal(Long orderId, Long confirmUserId) {
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getConfirmDealAt() != null) {
            throw new RuntimeException("该订单已落槌确认");
        }
        if (order.getOrderStatus() != 0 && order.getOrderStatus() != 1) {
            throw new RuntimeException("仅待付款或待发货订单可执行落槌确认");
        }
        order.setConfirmDealAt(LocalDateTime.now());
        order.setConfirmUserId(confirmUserId);
        order.setConfirmationNo("CJQS" + order.getOrderNo());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void shipOrder(Long orderId, String expressCompany, String expressNo) {
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new RuntimeException("仅待发货订单可执行发货");
        }
        if (expressCompany == null || expressCompany.trim().isEmpty() || expressNo == null || expressNo.trim().isEmpty()) {
            throw new RuntimeException("请填写快递公司和快递单号");
        }
        order.setExpressCompany(expressCompany.trim());
        order.setExpressNo(expressNo.trim());
        order.setShipTime(LocalDateTime.now());
        order.setOrderStatus(2);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderAsDefaulted(Long orderId) {
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("仅待付款订单可标记为悔拍");
        }
        BigDecimal depositAmount = order.getDepositAmount();
        if (depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            String reason = order.getOrderNo() != null
                ? "订单悔拍扣除，订单号：" + order.getOrderNo()
                : "订单悔拍扣除";
            depositService.deductForDefault(order.getBuyerId(), depositAmount, orderId, reason);
            String feeRemark = order.getOrderNo() != null
                ? "违规扣除保证金（悔拍），订单号：" + order.getOrderNo()
                : "违规扣除保证金（悔拍）";
            feeRecordService.recordDeduction(order.getBuyerId(), orderId, depositAmount, 3, feeRemark);
        }
        order.setOrderStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        AuctionGoods goods = goodsService.getById(order.getGoodsId());
        if (goods != null && goods.getDelFlag() == 0) {
            goods.setGoodsStatus(3);
            goods.setShelfStatus(0);
            goods.setAuditStatus(1);
            goods.setCurrentHighestPrice(null);
            goods.setUpdateTime(LocalDateTime.now());
            goodsService.updateById(goods);
        }
    }

    @Override
    public void confirmReceiptByBuyer(Long orderId, Long buyerId) {
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("仅买方本人可确认收货");
        }
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("仅待收货订单可确认收货");
        }
        order.setOrderStatus(3);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payByBuyer(Long orderId, Long buyerId) {
        AuctionOrder order = getById(orderId);
        if (order == null || order.getDelFlag() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("仅买方本人可支付该订单");
        }
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("仅待付款订单可支付");
        }
        // 假支付：仅将订单状态改为待发货，不扣保证金（保证金仅用于悔拍扣除）
        updateOrderStatus(orderId, 1);
    }

    @Override
    public PageInfo<Map<String, Object>> getOrdersPendingCommissionSettlement(Integer current, Integer size) {
        PageHelper.startPage(current, size);
        List<AuctionOrder> list = baseMapper.selectOrdersPendingCommissionSettlement();
        PageInfo<AuctionOrder> raw = new PageInfo<>(list);
        List<Map<String, Object>> rows = buildOrderRowsWithNames(raw.getList());
        PageInfo<Map<String, Object>> result = new PageInfo<>(rows);
        result.setTotal(raw.getTotal());
        result.setPages(raw.getPages());
        result.setPageNum(raw.getPageNum());
        result.setPageSize(raw.getPageSize());
        return result;
    }
}
