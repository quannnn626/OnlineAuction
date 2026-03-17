package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFeeRecord;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionFeeRecordMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFeeRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionFeeRecordServiceImpl extends ServiceImpl<AuctionFeeRecordMapper, AuctionFeeRecord> implements IAuctionFeeRecordService {

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
}
