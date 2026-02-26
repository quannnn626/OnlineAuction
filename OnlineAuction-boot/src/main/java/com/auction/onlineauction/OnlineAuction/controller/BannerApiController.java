package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionBanner;
import com.auction.onlineauction.OnlineAuction.service.IAuctionBannerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 轮播图公开 API（首页展示）
 * 无需登录，返回已启用的轮播图
 */
@RestController
@RequestMapping("/api/banner")
public class BannerApiController {

    @Autowired
    private IAuctionBannerService bannerService;

    @GetMapping("/list")
    public Result<List<AuctionBanner>> list() {
        try {
            QueryWrapper<AuctionBanner> q = new QueryWrapper<>();
            q.eq("del_flag", 0).eq("banner_status", 1).orderByAsc("banner_sort");
            return Result.success("获取成功", bannerService.list(q));
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}
