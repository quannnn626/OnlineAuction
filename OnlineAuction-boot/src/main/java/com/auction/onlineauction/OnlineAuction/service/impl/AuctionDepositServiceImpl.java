package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionDepositMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 保证金记录表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionDepositServiceImpl extends ServiceImpl<AuctionDepositMapper, AuctionDeposit> implements IAuctionDepositService {

    @Override
    public PageInfo<AuctionDeposit> getDepositPage(Integer current, Integer size, Long userId, Integer depositType) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionDeposit> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (userId != null) q.eq("user_id", userId);
        if (depositType != null) q.eq("deposit_type", depositType);
        q.orderByDesc("operate_time");
        List<AuctionDeposit> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    public BigDecimal getBalanceByUserId(Long userId) {
        QueryWrapper<AuctionDeposit> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("del_flag", 0).orderByDesc("operate_time").last("LIMIT 1");
        AuctionDeposit last = getOne(q);
        return last != null ? last.getBalance() : BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualTopUp(Long userId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        BigDecimal balance = getBalanceByUserId(userId);
        BigDecimal newBalance = balance.add(amount);
        AuctionDeposit record = new AuctionDeposit();
        record.setUserId(userId);
        record.setOrderId(null);
        record.setAmount(amount);
        record.setDepositType(0);
        record.setBalance(newBalance);
        record.setOperateTime(LocalDateTime.now());
        record.setRemark(remark != null ? remark : "财务手动充值");
        record.setDelFlag(0);
        save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeForBid(Long userId, BigDecimal amount, Long goodsId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal balance = getBalanceByUserId(userId);
        if (balance.compareTo(amount) < 0) {
            throw new RuntimeException("保证金余额不足，需冻结" + amount + "元，当前余额" + balance + "元");
        }
        BigDecimal newBalance = balance.subtract(amount);
        AuctionDeposit record = new AuctionDeposit();
        record.setUserId(userId);
        record.setOrderId(null);
        record.setAmount(amount);
        record.setDepositType(1);
        record.setBalance(newBalance);
        record.setOperateTime(LocalDateTime.now());
        record.setRemark(goodsId != null ? "参与竞拍商品ID=" + goodsId : "参与竞拍冻结");
        record.setDelFlag(0);
        save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductForDefault(Long userId, BigDecimal amount, Long orderId, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal balance = getBalanceByUserId(userId);
        BigDecimal newBalance = balance.subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        AuctionDeposit record = new AuctionDeposit();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setAmount(amount);
        record.setDepositType(4);
        record.setBalance(newBalance);
        record.setOperateTime(LocalDateTime.now());
        record.setRemark(remark != null ? remark : "订单悔拍扣除");
        record.setDelFlag(0);
        save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeByFinance(Long userId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("冻结金额必须大于0");
        }
        BigDecimal balance = getBalanceByUserId(userId);
        if (balance.compareTo(amount) < 0) {
            throw new RuntimeException("用户保证金余额不足，当前余额" + balance + "元");
        }
        BigDecimal newBalance = balance.subtract(amount);
        AuctionDeposit record = new AuctionDeposit();
        record.setUserId(userId);
        record.setOrderId(null);
        record.setAmount(amount);
        record.setDepositType(5);
        record.setBalance(newBalance);
        record.setOperateTime(LocalDateTime.now());
        record.setRemark(remark != null ? remark : "财务冻结");
        record.setDelFlag(0);
        save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeByFinance(Long userId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("解冻金额必须大于0");
        }
        BigDecimal balance = getBalanceByUserId(userId);
        BigDecimal newBalance = balance.add(amount);
        AuctionDeposit record = new AuctionDeposit();
        record.setUserId(userId);
        record.setOrderId(null);
        record.setAmount(amount);
        record.setDepositType(6);
        record.setBalance(newBalance);
        record.setOperateTime(LocalDateTime.now());
        record.setRemark(remark != null ? remark : "财务解冻");
        record.setDelFlag(0);
        save(record);
    }
}
