package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * <p>
 * 竞拍公告表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionNoticeService extends IService<AuctionNotice> {

    /**
     * 前台：分页获取已发布的公告（置顶优先，再按发布时间倒序）
     */
    PageInfo<AuctionNotice> getPublishedNoticePage(Integer current, Integer size, String keyword);

    /**
     * 后台：分页获取所有公告（含未发布、已下架）
     */
    PageInfo<AuctionNotice> getNoticePage(Integer current, Integer size, String keyword, Integer noticeStatus);
}
