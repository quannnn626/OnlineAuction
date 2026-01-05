package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.auction.onlineauction.OnlineAuction.service.IAuctionCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public Result<Page<AuctionCategory>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryName) {
        try {
            // 创建分页对象，确保统计总数
            Page<AuctionCategory> page = new Page<>(current, size);
            page.setSearchCount(true); // 确保统计总数
            
            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                wrapper.like("category_name", categoryName);
            }
            wrapper.orderByAsc("category_sort", "id");
            
            Page<AuctionCategory> result = categoryService.page(page, wrapper);
            
            // 确保total值正确（如果为0但有数据，可能是统计问题）
            if (result.getTotal() == 0 && result.getRecords() != null && !result.getRecords().isEmpty()) {
                // 如果total为0但有数据，重新统计
                long count = categoryService.count(wrapper);
                result.setTotal(count);
            }
            
            return Result.success("查询成功", result);
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
            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            wrapper.orderByAsc("category_sort", "id");
            List<AuctionCategory> list = categoryService.list(wrapper);
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
            AuctionCategory category = categoryService.getById(id);
            if (category == null || category.getDelFlag() == 1) {
                return Result.error("分类不存在");
            }
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
            // 验证分类名称
            if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
                return Result.error("分类名称不能为空");
            }
            // 检查分类名称是否重复
            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("category_name", category.getCategoryName());
            wrapper.eq("del_flag", 0);
            long count = categoryService.count(wrapper);
            if (count > 0) {
                return Result.error("分类名称已存在");
            }
            // 设置默认值
            if (category.getCategorySort() == null) {
                category.setCategorySort(0);
            }
            category.setCreateTime(LocalDateTime.now());
            category.setUpdateTime(LocalDateTime.now());
            category.setDelFlag(0);
            boolean success = categoryService.save(category);
            if (success) {
                return Result.success("新增成功", category);
            } else {
                return Result.error("新增失败");
            }
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
            // 验证分类是否存在
            AuctionCategory existing = categoryService.getById(id);
            if (existing == null || existing.getDelFlag() == 1) {
                return Result.error("分类不存在");
            }
            // 验证分类名称
            if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
                return Result.error("分类名称不能为空");
            }
            // 检查分类名称是否重复（排除自己）
            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("category_name", category.getCategoryName());
            wrapper.eq("del_flag", 0);
            wrapper.ne("id", id);
            long count = categoryService.count(wrapper);
            if (count > 0) {
                return Result.error("分类名称已存在");
            }
            // 设置更新信息
            category.setId(id);
            category.setUpdateTime(LocalDateTime.now());
            // 保留创建时间
            category.setCreateTime(existing.getCreateTime());
            // 保留删除标志
            category.setDelFlag(existing.getDelFlag());
            boolean success = categoryService.updateById(category);
            if (success) {
                return Result.success("更新成功", category);
            } else {
                return Result.error("更新失败");
            }
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
            AuctionCategory category = categoryService.getById(id);
            if (category == null || category.getDelFlag() == 1) {
                return Result.error("分类不存在");
            }
            // 逻辑删除
            category.setDelFlag(1);
            category.setUpdateTime(LocalDateTime.now());
            boolean success = categoryService.updateById(category);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
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
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的分类");
            }
            for (Long id : ids) {
                AuctionCategory category = categoryService.getById(id);
                if (category != null && category.getDelFlag() == 0) {
                    category.setDelFlag(1);
                    category.setUpdateTime(LocalDateTime.now());
                    categoryService.updateById(category);
                }
            }
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
}
