package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDepositRechargeApply;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionDepositRechargeApplyMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositRechargeApplyService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
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

@Service
public class AuctionDepositRechargeApplyServiceImpl extends ServiceImpl<AuctionDepositRechargeApplyMapper, AuctionDepositRechargeApply> implements IAuctionDepositRechargeApplyService {

    @Autowired
    private IAuctionDepositService depositService;

    @Override
    public PageInfo<AuctionDepositRechargeApply> getMyPage(Integer current, Integer size, Long userId) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionDepositRechargeApply> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("del_flag", 0).orderByDesc("apply_time");
        List<AuctionDepositRechargeApply> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AuctionDepositRechargeApply> getAdminPage(Integer current, Integer size, Integer status) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionDepositRechargeApply> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (status != null) q.eq("status", status);
        q.orderByDesc("apply_time");
        List<AuctionDepositRechargeApply> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(Long userId, BigDecimal amount, String applyRemark) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("参数不完整或金额必须大于0");
        }
        AuctionDepositRechargeApply apply = new AuctionDepositRechargeApply();
        apply.setUserId(userId);
        apply.setAmount(amount);
        apply.setStatus(0);
        apply.setApplyRemark(applyRemark);
        apply.setApplyTime(LocalDateTime.now());
        apply.setDelFlag(0);
        save(apply);
        return apply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long handleUserId, String handleRemark) {
        AuctionDepositRechargeApply apply = getById(id);
        if (apply == null || apply.getDelFlag() == 1) {
            throw new RuntimeException("申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }
        depositService.manualTopUp(apply.getUserId(), apply.getAmount(), "充值申请通过，申请单号:" + id);
        apply.setStatus(1);
        apply.setHandleUserId(handleUserId);
        apply.setHandleTime(LocalDateTime.now());
        apply.setHandleRemark(handleRemark);
        updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, Long handleUserId, String handleRemark) {
        AuctionDepositRechargeApply apply = getById(id);
        if (apply == null || apply.getDelFlag() == 1) {
            throw new RuntimeException("申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }
        apply.setStatus(2);
        apply.setHandleUserId(handleUserId);
        apply.setHandleTime(LocalDateTime.now());
        apply.setHandleRemark(handleRemark);
        updateById(apply);
    }
}
