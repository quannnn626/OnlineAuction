package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.auction.onlineauction.OnlineAuction.service.IAuctionCategoryService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
     * 分页查询商品分类列表（后台，需管理员/运营等角色）
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionCategory>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryName,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            PageInfo<AuctionCategory> pageInfo = categoryService.getCategoryPage(current, size, categoryName);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有商品分类列表（后台，不分页，用于下拉选择等）
     */
    @GetMapping("/list")
    public Result<List<AuctionCategory>> getCategoryList(HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            List<AuctionCategory> list = categoryService.getCategoryList();
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品分类详情（后台）
     */
    @GetMapping("/{id}")
    public Result<AuctionCategory> getCategoryById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            AuctionCategory category = categoryService.getCategoryById(id);
            return Result.success("查询成功", category);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增商品分类（管理员、超级管理员、运营可操作）
     */
    @PostMapping
    public Result<AuctionCategory> addCategory(@RequestBody AuctionCategory category,
                                               HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageCategoryOrBanner(request.getSession(false))) {
                return Result.error("无权限新增商品分类");
            }
            AuctionCategory createdCategory = categoryService.addCategory(category);
            return Result.success("新增成功", createdCategory);
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品分类（管理员、超级管理员、运营可操作）
     */
    @PutMapping("/{id}")
    public Result<AuctionCategory> updateCategory(@PathVariable Long id, @RequestBody AuctionCategory category,
                                                  HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageCategoryOrBanner(request.getSession(false))) {
                return Result.error("无权限编辑商品分类");
            }
            AuctionCategory updatedCategory = categoryService.updateCategory(id, category);
            return Result.success("更新成功", updatedCategory);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品分类（逻辑删除，仅管理员、超级管理员可操作）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canDeleteCategory(request.getSession(false))) {
                return Result.error("无权限删除商品分类");
            }
            categoryService.deleteCategory(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除商品分类（仅管理员、超级管理员可操作）
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteCategory(@RequestBody List<Long> ids, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canDeleteCategory(request.getSession(false))) {
                return Result.error("无权限批量删除商品分类");
            }
            categoryService.batchDeleteCategory(ids);
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品分类树形结构（后台，需管理员/运营等角色）
     */
    @GetMapping("/tree")
    public Result<List<AuctionCategory>> getCategoryTree(
            @RequestParam(required = false, defaultValue = "false") Boolean includeDisabled,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            List<AuctionCategory> tree = categoryService.getCategoryTree(includeDisabled);
            return Result.success("查询成功", tree);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据父ID获取子分类列表（后台）
     */
    @GetMapping("/children/{parentId}")
    public Result<List<AuctionCategory>> getChildrenByParentId(@PathVariable Long parentId,
                                                              HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            List<AuctionCategory> children = categoryService.getChildrenByParentId(parentId);
            return Result.success("查询成功", children);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}