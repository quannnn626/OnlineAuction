package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionNotice;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionNoticeMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionNoticeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 竞拍公告表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionNoticeServiceImpl extends ServiceImpl<AuctionNoticeMapper, AuctionNotice> implements IAuctionNoticeService {

    @Override
    public PageInfo<AuctionNotice> getPublishedNoticePage(Integer current, Integer size, String keyword) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionNotice> q = new QueryWrapper<>();
        q.eq("del_flag", 0).eq("notice_status", 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            q.and(w -> w.like("notice_title", keyword).or().like("notice_content", keyword));
        }
        q.orderByDesc("is_top", "publish_time");
        List<AuctionNotice> list = list(q);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AuctionNotice> getNoticePage(Integer current, Integer size, String keyword, Integer noticeStatus) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionNotice> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            q.and(w -> w.like("notice_title", keyword).or().like("notice_content", keyword));
        }
        if (noticeStatus != null) {
            q.eq("notice_status", noticeStatus);
        }
        q.orderByDesc("is_top", "create_time");
        List<AuctionNotice> list = list(q);
        return new PageInfo<>(list);
    }
}
