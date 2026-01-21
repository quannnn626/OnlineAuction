package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * <p>
 * 拍卖商品类目表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionCategoryService extends IService<AuctionCategory> {

    /**
     * 分页查询商品分类列表
     */
    PageInfo<AuctionCategory> getCategoryPage(Integer current, Integer size, String categoryName);

    /**
     * 获取所有商品分类列表（不分页）
     */
    List<AuctionCategory> getCategoryList();

    /**
     * 根据ID获取商品分类详情
     */
    AuctionCategory getCategoryById(Long id);

    /**
     * 新增商品分类
     */
    AuctionCategory addCategory(AuctionCategory category);

    /**
     * 更新商品分类
     */
    AuctionCategory updateCategory(Long id, AuctionCategory category);

    /**
     * 删除商品分类（逻辑删除）
     */
    void deleteCategory(Long id);

    /**
     * 批量删除商品分类
     */
    void batchDeleteCategory(List<Long> ids);

    /**
     * 获取商品分类树形结构
     * @param includeDisabled 是否包含禁用的分类
     * @return 树形结构的分类列表
     */
    List<AuctionCategory> getCategoryTree(boolean includeDisabled);

    /**
     * 根据父ID获取子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<AuctionCategory> getChildrenByParentId(Long parentId);

    /**
     * 检查分类是否可以删除（无子分类且无关联商品）
     * @param categoryId 分类ID
     * @return true 如果可以删除，false 否则
     */
    boolean canDelete(Long categoryId);
}
