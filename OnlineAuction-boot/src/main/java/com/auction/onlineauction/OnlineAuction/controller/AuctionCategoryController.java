package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.auction.onlineauction.OnlineAuction.service.IAuctionCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            // 使用 PageHelper 开始分页
            PageHelper.startPage(current, size);

            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                wrapper.like("category_name", categoryName);
            }
            wrapper.orderByAsc("category_sort", "id");

            // 查询数据（此时会自动分页）
            List<AuctionCategory> list = categoryService.list(wrapper);

            // 使用 PageInfo 包装结果
            PageInfo<AuctionCategory> pageInfo = new PageInfo<>(list);

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
            
            // 处理父分类ID和层级
            Long parentId = category.getParentId();
            if (parentId == null) {
                parentId = 0L;
            }
            category.setParentId(parentId);
            
            // 计算层级
            Integer level = 1;
            if (parentId != null && parentId > 0) {
                AuctionCategory parent = categoryService.getById(parentId);
                if (parent == null || parent.getDelFlag() == 1) {
                    return Result.error("父分类不存在");
                }
                level = parent.getLevel() + 1;
                // 限制最多三级
                if (level > 3) {
                    return Result.error("分类层级最多为三级，无法继续添加");
                }
            }
            category.setLevel(level);
            
            // 检查分类名称是否重复（同一父分类下）
            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("category_name", category.getCategoryName());
            wrapper.eq("parent_id", parentId);
            wrapper.eq("del_flag", 0);
            long count = categoryService.count(wrapper);
            if (count > 0) {
                return Result.error("同级分类名称已存在");
            }
            
            // 设置默认值
            if (category.getCategorySort() == null) {
                category.setCategorySort(0);
            }
            if (category.getCategoryStatus() == null) {
                category.setCategoryStatus(1); // 默认启用
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
            
            // 处理父分类ID和层级（如果修改了父分类）
            Long parentId = category.getParentId();
            if (parentId == null) {
                parentId = existing.getParentId();
            }
            
            // 如果修改了父分类，需要重新计算层级
            Integer level = existing.getLevel();
            if (!parentId.equals(existing.getParentId())) {
                if (parentId == null || parentId == 0) {
                    level = 1;
                } else {
                    AuctionCategory parent = categoryService.getById(parentId);
                    if (parent == null || parent.getDelFlag() == 1) {
                        return Result.error("父分类不存在");
                    }
                    level = parent.getLevel() + 1;
                    // 限制最多三级
                    if (level > 3) {
                        return Result.error("分类层级最多为三级，无法继续添加");
                    }
                }
                category.setParentId(parentId);
                category.setLevel(level);
            }
            
            // 检查分类名称是否重复（同一父分类下，排除自己）
            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("category_name", category.getCategoryName());
            wrapper.eq("parent_id", parentId);
            wrapper.eq("del_flag", 0);
            wrapper.ne("id", id);
            long count = categoryService.count(wrapper);
            if (count > 0) {
                return Result.error("同级分类名称已存在");
            }
            
            // 设置更新信息
            category.setId(id);
            category.setUpdateTime(LocalDateTime.now());
            // 保留创建时间
            category.setCreateTime(existing.getCreateTime());
            // 保留删除标志
            category.setDelFlag(existing.getDelFlag());
            // 如果没有传递level，保留原有的
            if (category.getLevel() == null) {
                category.setLevel(existing.getLevel());
            }
            // 如果没有传递parentId，保留原有的
            if (category.getParentId() == null) {
                category.setParentId(existing.getParentId());
            }
            // 如果没有传递categoryStatus，保留原有的
            if (category.getCategoryStatus() == null) {
                category.setCategoryStatus(existing.getCategoryStatus());
            }
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
            // 检查是否可以删除
            if (!categoryService.canDelete(id)) {
                return Result.error("该分类存在子分类或关联商品，无法删除");
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
                    // 检查是否可以删除
                    if (!categoryService.canDelete(id)) {
                        return Result.error("分类\"" + category.getCategoryName() + "\"存在子分类或关联商品，无法删除");
                    }
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

    /**
     * 获取商品分类树形结构（用于树形控件展示，管理页面使用，包含所有分类包括禁用的）
     */
    @GetMapping("/tree")
    public Result<List<AuctionCategory>> getCategoryTree(
            @RequestParam(required = false, defaultValue = "false") Boolean includeDisabled) {
        try {
            QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (!includeDisabled) {
                wrapper.eq("category_status", 1); // 默认只获取启用的分类
            }
            wrapper.orderByAsc("category_sort", "id");
            List<AuctionCategory> allCategories = categoryService.list(wrapper);
            
            // 构建树形结构
            List<AuctionCategory> tree = buildCategoryTree(allCategories);
            return Result.success("查询成功", tree);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 递归构建分类树
     */
    private List<AuctionCategory> buildCategoryTree(List<AuctionCategory> allCategories) {
        List<AuctionCategory> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId() == null || category.getParentId() == 0)
                .collect(Collectors.toList());

        for (AuctionCategory root : rootCategories) {
            root.setChildren(findCategoryChildren(root.getId(), allCategories));
        }
        return rootCategories;
    }

    private List<AuctionCategory> findCategoryChildren(Long parentId, List<AuctionCategory> allCategories) {
        List<AuctionCategory> children = allCategories.stream()
                .filter(category -> parentId != null && parentId.equals(category.getParentId()))
                .collect(Collectors.toList());

        for (AuctionCategory child : children) {
            child.setChildren(findCategoryChildren(child.getId(), allCategories));
        }
        return children;
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