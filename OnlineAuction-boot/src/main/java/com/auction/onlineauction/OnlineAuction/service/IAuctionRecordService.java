package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /**
     * 查询指定商品当前最高价记录
     */
    AuctionRecord getHighestRecordByGoodsId(Long goodsId);

    /**
     * 用户历史竞拍商品（按商品聚合）
     */
    PageInfo<Map<String, Object>> getMyBidGoodsPage(Integer current, Integer size, Long buyerId, String keyword);

    /**
     * 后台历史竞拍商品列表（按商品聚合）
     */
    PageInfo<Map<String, Object>> getAdminBidGoodsPage(Integer current, Integer size, String keyword);

    /**
     * 后台查看某商品下的竞拍记录详情
     */
    PageInfo<AuctionRecord> getBidRecordsByGoodsPage(Integer current, Integer size, Long goodsId);

    /**
     * 用户查看自己在某商品下的竞拍记录详情
     */
    PageInfo<AuctionRecord> getMyBidRecordsByGoodsPage(Integer current, Integer size, Long goodsId, Long buyerId);

    /** 拍卖师/管理员标记异常出价：0=正常 1=恶意出价 2=机器人 */
    void markAbnormal(Long recordId, Integer abnormalType);

    /** 按买方分页查询竞拍记录（含商品名，用于保证金管理） */
    PageInfo<AuctionRecord> getRecordsByBuyerIdPage(Integer current, Integer size, Long buyerId);
}
