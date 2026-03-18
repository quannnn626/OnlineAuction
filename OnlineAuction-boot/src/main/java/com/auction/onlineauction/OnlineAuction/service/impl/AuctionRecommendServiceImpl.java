package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionRecommend;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionRecommendMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionRecommendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionRecommendServiceImpl extends ServiceImpl<AuctionRecommendMapper, AuctionRecommend> implements IAuctionRecommendService {

    @Override
    public List<AuctionRecommend> listByTypeAndTarget(String recommendType, Long targetId) {
        QueryWrapper<AuctionRecommend> q = new QueryWrapper<>();
        q.eq("del_flag", 0).eq("recommend_type", recommendType);
        if (targetId != null) q.eq("target_id", targetId);
        q.orderByAsc("sort_order");
        return list(q);
    }

    @Override
    public void updateSortOrder(Long id, Integer sortOrder) {
        AuctionRecommend r = getById(id);
        if (r != null && sortOrder != null) {
            r.setSortOrder(sortOrder);
            updateById(r);
        }
    }
}
