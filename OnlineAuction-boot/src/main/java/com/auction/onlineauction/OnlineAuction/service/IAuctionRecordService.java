package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 竞拍记录表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionRecordService extends IService<AuctionRecord> {

    /**
     * 提交竞拍出价（使用数据库锁保证原子性）
     * @param goodsId 商品ID
     * @param buyerId 买方用户ID
     * @param bidPrice 出价金额
     * @return 竞拍记录
     */
    AuctionRecord submitBid(Long goodsId, Long buyerId, BigDecimal bidPrice);

    /**
     * 查询商品的竞拍记录列表
     * @param goodsId 商品ID
     * @param limit 限制数量
     * @return 竞拍记录列表
     */
    List<AuctionRecord> getRecordsByGoodsId(Long goodsId, Integer limit);
}
