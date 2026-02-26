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
}
