package com.auction.onlineauction.OnlineAuction.config;

import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商品状态定时任务
 * 定期自动下架超过结束时间的商品
 *
 * @author MrYan
 * @since 2026-02-08
 */
@Component
public class GoodsStatusScheduler {

    @Autowired
    private IAuctionGoodsService goodsService;

    /**
     * 每5分钟执行一次，自动下架超过结束时间的商品
     */
    @Scheduled(fixedRate = 300000) // 300000毫秒 = 5分钟
    public void autoOfflineExpiredGoods() {
        try {
            goodsService.autoOfflineExpiredGoods();
        } catch (Exception e) {
            System.err.println("自动下架商品任务执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
