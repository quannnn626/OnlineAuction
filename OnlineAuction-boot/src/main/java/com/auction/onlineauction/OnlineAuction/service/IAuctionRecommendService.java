package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionRecommend;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 推荐位（首页推荐、分类推荐）
 */
public interface IAuctionRecommendService extends IService<AuctionRecommend> {

    List<AuctionRecommend> listByTypeAndTarget(String recommendType, Long targetId);

    void updateSortOrder(Long id, Integer sortOrder);
}
