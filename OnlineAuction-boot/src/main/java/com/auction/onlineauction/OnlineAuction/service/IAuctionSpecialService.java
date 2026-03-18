package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionSpecial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 拍卖专场
 */
public interface IAuctionSpecialService extends IService<AuctionSpecial> {

    List<Map<String, Object>> listGoodsBySpecialId(Long specialId);

    void addGoods(Long specialId, Long goodsId, Integer sortOrder);

    void removeGoods(Long specialId, Long goodsId);
}
