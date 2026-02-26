package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionBanner;
import com.auction.onlineauction.OnlineAuction.service.IAuctionBannerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 首页轮播图管理（后台）
 * 查看：管理员(3)、超级管理员(4)、拍卖师(5)、客服(6)、财务(7)、运营(8)
 * 增改：管理员(3)、超级管理员(4)、运营(8)
 * 删除：仅管理员(3)、超级管理员(4)
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionBanner")
public class AuctionBannerController {

    @Autowired
    private IAuctionBannerService bannerService;

    @GetMapping("/list")
    public Result<List<AuctionBanner>> list(HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canViewBanner(request.getSession(false))) {
                return Result.error("无权限查看轮播图");
            }
            QueryWrapper<AuctionBanner> q = new QueryWrapper<>();
            q.eq("del_flag", 0).orderByAsc("banner_sort");
            return Result.success("查询成功", bannerService.list(q));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<AuctionBanner> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canViewBanner(request.getSession(false))) {
                return Result.error("无权限查看轮播图");
            }
            AuctionBanner b = bannerService.getById(id);
            if (b == null || b.getDelFlag() == 1) {
                return Result.error("轮播图不存在");
            }
            return Result.success("查询成功", b);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<AuctionBanner> add(@RequestBody AuctionBanner banner, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageBanner(request.getSession(false))) {
                return Result.error("无权限新增轮播图");
            }
            if (banner.getBannerStatus() == null) banner.setBannerStatus(0);
            banner.setDelFlag(0);
            bannerService.save(banner);
            return Result.success("新增成功", banner);
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<AuctionBanner> update(@PathVariable Long id, @RequestBody AuctionBanner banner,
                                       HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageBanner(request.getSession(false))) {
                return Result.error("无权限编辑轮播图");
            }
            AuctionBanner exist = bannerService.getById(id);
            if (exist == null || exist.getDelFlag() == 1) {
                return Result.error("轮播图不存在");
            }
            banner.setId(id);
            bannerService.updateById(banner);
            return Result.success("更新成功", bannerService.getById(id));
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canDeleteBanner(request.getSession(false))) {
                return Result.error("无权限删除轮播图");
            }
            AuctionBanner b = bannerService.getById(id);
            if (b == null || b.getDelFlag() == 1) {
                return Result.error("轮播图不存在");
            }
            b.setDelFlag(1);
            bannerService.updateById(b);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
