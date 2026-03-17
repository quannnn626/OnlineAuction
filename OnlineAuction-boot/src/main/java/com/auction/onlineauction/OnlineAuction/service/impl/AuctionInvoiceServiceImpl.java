package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionInvoice;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionInvoiceMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionInvoiceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuctionInvoiceServiceImpl extends ServiceImpl<AuctionInvoiceMapper, AuctionInvoice> implements IAuctionInvoiceService {

    @Override
    public PageInfo<AuctionInvoice> getMyPage(Integer current, Integer size, Long userId) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionInvoice> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("del_flag", 0).orderByDesc("apply_time");
        List<AuctionInvoice> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AuctionInvoice> getAdminPage(Integer current, Integer size, Integer status, Long userId) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionInvoice> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (status != null) q.eq("status", status);
        if (userId != null) q.eq("user_id", userId);
        q.orderByDesc("apply_time");
        List<AuctionInvoice> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(Long userId, Long orderId, String invoiceTitle, String taxNo, BigDecimal amount, Integer invoiceType) {
        if (userId == null || invoiceTitle == null || invoiceTitle.trim().isEmpty() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("参数不完整");
        }
        if (invoiceType == null || (invoiceType != 1 && invoiceType != 2)) {
            invoiceType = 1;
        }
        AuctionInvoice inv = new AuctionInvoice();
        inv.setUserId(userId);
        inv.setOrderId(orderId);
        inv.setInvoiceTitle(invoiceTitle.trim());
        inv.setTaxNo(taxNo != null ? taxNo.trim() : null);
        inv.setAmount(amount);
        inv.setInvoiceType(invoiceType);
        inv.setStatus(0);
        inv.setApplyTime(LocalDateTime.now());
        inv.setDelFlag(0);
        save(inv);
        return inv.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleInvoice(Long id, Integer status, Long fileId, String handleRemark, Long handleUserId) {
        AuctionInvoice inv = getById(id);
        if (inv == null || inv.getDelFlag() == 1) {
            throw new RuntimeException("发票申请不存在");
        }
        if (inv.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new RuntimeException("状态无效（1=已开票 2=已驳回）");
        }
        if (status == 1 && (fileId == null || fileId <= 0)) {
            throw new RuntimeException("已开票需上传发票文件");
        }
        inv.setStatus(status);
        inv.setFileId(status == 1 ? fileId : null);
        inv.setHandleRemark(handleRemark);
        inv.setHandleUserId(handleUserId);
        inv.setHandleTime(LocalDateTime.now());
        updateById(inv);
    }
}
