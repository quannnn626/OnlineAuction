package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionCategoryMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionCategoryService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 拍卖商品类目表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionCategoryServiceImpl extends ServiceImpl<AuctionCategoryMapper, AuctionCategory> implements IAuctionCategoryService {

    @Autowired
    private IAuctionGoodsService goodsService; // 注入商品Service用于检查关联商品

    @Override
    public List<AuctionCategory> getCategoryTree() {
        QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.eq("category_status", 1); // 只获取启用的分类
        wrapper.orderByAsc("category_sort", "id");
        List<AuctionCategory> allCategories = list(wrapper);

        // 构建树形结构
        return buildTree(allCategories);
    }

    @Override
    public List<AuctionCategory> getChildrenByParentId(Long parentId) {
        QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId);
        wrapper.eq("del_flag", 0);
        wrapper.eq("category_status", 1); // 只获取启用的分类
        wrapper.orderByAsc("category_sort", "id");
        return list(wrapper);
    }

    @Override
    public boolean canDelete(Long categoryId) {
        // 1. 检查是否有子分类
        QueryWrapper<AuctionCategory> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", categoryId);
        childWrapper.eq("del_flag", 0);
        if (count(childWrapper) > 0) {
            return false; // 有子分类，不能删除
        }

        // 2. 检查是否有关联商品
        QueryWrapper<AuctionGoods> goodsWrapper = new QueryWrapper<>();
        // category_id 字段存储的是逗号分隔的字符串，需要使用 LIKE 或 FIND_IN_SET
        goodsWrapper.apply("FIND_IN_SET({0}, category_id)", categoryId);
        goodsWrapper.eq("del_flag", 0);
        if (goodsService.count(goodsWrapper) > 0) {
            return false; // 有关联商品，不能删除
        }

        return true; // 没有子分类也没有关联商品，可以删除
    }

    /**
     * 递归构建分类树
     */
    private List<AuctionCategory> buildTree(List<AuctionCategory> allCategories) {
        List<AuctionCategory> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId() == null || category.getParentId() == 0)
                .collect(Collectors.toList());

        for (AuctionCategory root : rootCategories) {
            root.setChildren(findChildren(root.getId(), allCategories));
        }
        return rootCategories;
    }

    private List<AuctionCategory> findChildren(Long parentId, List<AuctionCategory> allCategories) {
        List<AuctionCategory> children = allCategories.stream()
                .filter(category -> parentId != null && parentId.equals(category.getParentId()))
                .collect(Collectors.toList());

        for (AuctionCategory child : children) {
            child.setChildren(findChildren(child.getId(), allCategories));
        }
        return children;
    }
}
