package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.auction.onlineauction.OnlineAuction.service.IAuctionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 商品分类API控制器（供前端调用）
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/category")
public class CategoryApiController {

    @Autowired
    private IAuctionCategoryService categoryService;

    /**
     * 获取所有商品分类列表（用于首页展示等）
     * @return 分类列表
     */
    @GetMapping("/list")
    public Result<List<AuctionCategory>> getCategoryList() {
        try {
            List<AuctionCategory> list = categoryService.getCategoryList();
            return Result.success("获取成功", list);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}

