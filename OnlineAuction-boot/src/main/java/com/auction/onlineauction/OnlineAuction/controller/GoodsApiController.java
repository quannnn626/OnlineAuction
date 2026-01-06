package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) Integer status) {
        try {
            // 使用 PageHelper 开始分页
            PageHelper.startPage(current, size);

            QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            wrapper.eq("audit_status", 1); // 只显示审核通过的商品
            
            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("goods_name", keyword);
            }
            
            // 状态筛选
            if (status != null) {
                wrapper.eq("goods_status", status);
            }
            
            wrapper.orderByDesc("create_time");

            // 查询数据（此时会自动分页）
            List<AuctionGoods> list = goodsService.list(wrapper);

            // 使用 PageInfo 包装结果
            PageInfo<AuctionGoods> pageInfo = new PageInfo<>(list);

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
            // 使用 PageHelper 开始分页
            PageHelper.startPage(current, size);

            QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            wrapper.eq("audit_status", 1); // 只显示审核通过的商品
            
            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("goods_name", keyword).or().like("goods_desc", keyword);
            }
            
            wrapper.orderByDesc("create_time");

            // 查询数据（此时会自动分页）
            List<AuctionGoods> list = goodsService.list(wrapper);

            // 使用 PageInfo 包装结果
            PageInfo<AuctionGoods> pageInfo = new PageInfo<>(list);

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
            // 使用 PageHelper 开始分页
            PageHelper.startPage(current, size);

            QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            wrapper.eq("seller_id", userId);
            
            wrapper.orderByDesc("create_time");

            // 查询数据（此时会自动分页）
            List<AuctionGoods> list = goodsService.list(wrapper);

            // 使用 PageInfo 包装结果
            PageInfo<AuctionGoods> pageInfo = new PageInfo<>(list);

            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<AuctionGoods> getGoodsById(@PathVariable Long id) {
        try {
            AuctionGoods goods = goodsService.getById(id);
            if (goods == null || goods.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
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
            AuctionGoods goods = goodsService.getById(id);
            if (goods == null || goods.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            // 逻辑删除
            goods.setDelFlag(1);
            boolean success = goodsService.updateById(goods);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
