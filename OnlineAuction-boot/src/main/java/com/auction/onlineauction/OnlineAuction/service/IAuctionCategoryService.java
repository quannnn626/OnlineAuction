package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 获取所有商品分类的树形结构
     * @return 树形结构的分类列表
     */
    List<AuctionCategory> getCategoryTree();

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
