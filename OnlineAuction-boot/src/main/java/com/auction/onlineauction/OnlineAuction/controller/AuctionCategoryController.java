package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.auction.onlineauction.OnlineAuction.service.IAuctionCategoryService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 拍卖商品类目表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionCategory")
public class AuctionCategoryController {

    @Autowired
    private IAuctionCategoryService categoryService;

    /**
     * 分页查询商品分类列表
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionCategory>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryName) {
        try {
            PageInfo<AuctionCategory> pageInfo = categoryService.getCategoryPage(current, size, categoryName);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有商品分类列表（不分页，用于下拉选择等）
     */
    @GetMapping("/list")
    public Result<List<AuctionCategory>> getCategoryList() {
        try {
            List<AuctionCategory> list = categoryService.getCategoryList();
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品分类详情
     */
    @GetMapping("/{id}")
    public Result<AuctionCategory> getCategoryById(@PathVariable Long id) {
        try {
            AuctionCategory category = categoryService.getCategoryById(id);
            return Result.success("查询成功", category);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增商品分类
     */
    @PostMapping
    public Result<AuctionCategory> addCategory(@RequestBody AuctionCategory category) {
        try {
            AuctionCategory createdCategory = categoryService.addCategory(category);
            return Result.success("新增成功", createdCategory);
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品分类
     */
    @PutMapping("/{id}")
    public Result<AuctionCategory> updateCategory(@PathVariable Long id, @RequestBody AuctionCategory category) {
        try {
            AuctionCategory updatedCategory = categoryService.updateCategory(id, category);
            return Result.success("更新成功", updatedCategory);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品分类（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除商品分类
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteCategory(@RequestBody List<Long> ids) {
        try {
            categoryService.batchDeleteCategory(ids);
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品分类树形结构（用于树形控件展示，管理页面使用，包含所有分类包括禁用的）
     */
    @GetMapping("/tree")
    public Result<List<AuctionCategory>> getCategoryTree(
            @RequestParam(required = false, defaultValue = "false") Boolean includeDisabled) {
        try {
            List<AuctionCategory> tree = categoryService.getCategoryTree(includeDisabled);
            return Result.success("查询成功", tree);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据父ID获取子分类列表
     */
    @GetMapping("/children/{parentId}")
    public Result<List<AuctionCategory>> getChildrenByParentId(@PathVariable Long parentId) {
        try {
            List<AuctionCategory> children = categoryService.getChildrenByParentId(parentId);
            return Result.success("查询成功", children);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}