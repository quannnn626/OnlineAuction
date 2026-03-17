package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFeeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

/**
 * 平台扣费/佣金记录（财务查看）
 */
public interface IAuctionFeeRecordService extends IService<AuctionFeeRecord> {

    PageInfo<AuctionFeeRecord> getPage(Integer current, Integer size, Integer feeType, Integer status, Long userId);
}
