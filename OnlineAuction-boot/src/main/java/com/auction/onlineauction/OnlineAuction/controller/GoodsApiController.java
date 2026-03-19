package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.github.pagehelper.PageInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商品API控制器（供前端调用）
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/goods")
public class GoodsApiController extends BaseApiController {

    @Autowired
    private IAuctionGoodsService goodsService;

    /**
     * 获取商品列表（支持分页、搜索、状态筛选）
     */
    @GetMapping("/list")
    public Result<PageInfo<AuctionGoods>> getGoodsList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String categoryId) {
        try {
            PageInfo<AuctionGoods> pageInfo = goodsService.getGoodsListForApi(current, size, keyword, status, categoryId);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 搜索商品（支持分页、关键词搜索）
     */
    @GetMapping("/search")
    public Result<PageInfo<AuctionGoods>> searchGoods(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            PageInfo<AuctionGoods> pageInfo = goodsService.searchGoods(current, size, keyword);
            return Result.success("搜索成功", pageInfo);
        } catch (Exception e) {
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的商品列表（支持分页）
     */
    @GetMapping("/my")
    public Result<PageInfo<AuctionGoods>> getMyGoodsList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long userId) {
        try {
            PageInfo<AuctionGoods> pageInfo = goodsService.getMyGoodsList(current, size, userId);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 热门商品分页：每页50条，按点击量倒序，支持翻页
     */
    @GetMapping("/hot")
    public Result<PageInfo<AuctionGoods>> getHotGoods(
            @RequestParam(defaultValue = "1") Integer current) {
        try {
            PageInfo<AuctionGoods> pageInfo = goodsService.getHotGoodsPage(current);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 批量获取商品详情（前台推荐用，不会增加点击量）
     * @param ids 逗号分隔商品ID，如：1,2,3
     */
    @GetMapping("/batch")
    public Result<List<AuctionGoods>> getGoodsBatch(@RequestParam String ids) {
        try {
            if (ids == null || ids.trim().isEmpty()) {
                return Result.success("查询成功", new ArrayList<>());
            }
            List<AuctionGoods> result = new ArrayList<>();
            Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(s -> {
                        try {
                            Long id = Long.valueOf(s);
                            AuctionGoods goods = goodsService.getGoodsByIdForPublic(id);
                            result.add(goods);
                        } catch (Exception ignore) {
                            // 跳过不存在/不可见的商品
                        }
                    });
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品详情（仅已上架且审核通过的商品），访问时增加点击量
     */
    @GetMapping("/{id}")
    public Result<AuctionGoods> getGoodsById(@PathVariable Long id) {
        try {
            goodsService.incrementViewCount(id);
            AuctionGoods goods = goodsService.getGoodsByIdForPublic(id);
            return Result.success("查询成功", goods);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        try {
            goodsService.deleteGoods(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
