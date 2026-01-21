package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionCategory;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionCategoryMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionCategoryService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public PageInfo<AuctionCategory> getCategoryPage(Integer current, Integer size, String categoryName) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            wrapper.like("category_name", categoryName);
        }
        wrapper.orderByAsc("category_sort", "id");

        List<AuctionCategory> list = list(wrapper);
        return new PageInfo<>(list);
    }

    @Override
    public List<AuctionCategory> getCategoryList() {
        QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.orderByAsc("category_sort", "id");
        return list(wrapper);
    }

    @Override
    public AuctionCategory getCategoryById(Long id) {
        AuctionCategory category = getById(id);
        if (category == null || category.getDelFlag() == 1) {
            throw new RuntimeException("分类不存在");
        }
        return category;
    }

    @Override
    public AuctionCategory addCategory(AuctionCategory category) {
        // 验证分类名称
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
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
            AuctionCategory parent = getById(parentId);
            if (parent == null || parent.getDelFlag() == 1) {
                throw new RuntimeException("父分类不存在");
            }
            level = parent.getLevel() + 1;
            // 限制最多三级
            if (level > 3) {
                throw new RuntimeException("分类层级最多为三级，无法继续添加");
            }
        }
        category.setLevel(level);
        
        // 检查分类名称是否重复（同一父分类下）
        QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("category_name", category.getCategoryName());
        wrapper.eq("parent_id", parentId);
        wrapper.eq("del_flag", 0);
        if (count(wrapper) > 0) {
            throw new RuntimeException("同级分类名称已存在");
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
        
        boolean success = save(category);
        if (!success) {
            throw new RuntimeException("新增失败");
        }
        return category;
    }

    @Override
    public AuctionCategory updateCategory(Long id, AuctionCategory category) {
        // 验证分类是否存在
        AuctionCategory existing = getById(id);
        if (existing == null || existing.getDelFlag() == 1) {
            throw new RuntimeException("分类不存在");
        }
        // 验证分类名称
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
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
                AuctionCategory parent = getById(parentId);
                if (parent == null || parent.getDelFlag() == 1) {
                    throw new RuntimeException("父分类不存在");
                }
                level = parent.getLevel() + 1;
                // 限制最多三级
                if (level > 3) {
                    throw new RuntimeException("分类层级最多为三级，无法继续添加");
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
        if (count(wrapper) > 0) {
            throw new RuntimeException("同级分类名称已存在");
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
        
        boolean success = updateById(category);
        if (!success) {
            throw new RuntimeException("更新失败");
        }
        return category;
    }

    @Override
    public void deleteCategory(Long id) {
        AuctionCategory category = getById(id);
        if (category == null || category.getDelFlag() == 1) {
            throw new RuntimeException("分类不存在");
        }
        // 检查是否可以删除
        if (!canDelete(id)) {
            throw new RuntimeException("该分类存在子分类或关联商品，无法删除");
        }
        // 逻辑删除
        category.setDelFlag(1);
        category.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(category);
        if (!success) {
            throw new RuntimeException("删除失败");
        }
    }

    @Override
    public void batchDeleteCategory(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择要删除的分类");
        }
        for (Long id : ids) {
            AuctionCategory category = getById(id);
            if (category != null && category.getDelFlag() == 0) {
                // 检查是否可以删除
                if (!canDelete(id)) {
                    throw new RuntimeException("分类\"" + category.getCategoryName() + "\"存在子分类或关联商品，无法删除");
                }
                category.setDelFlag(1);
                category.setUpdateTime(LocalDateTime.now());
                updateById(category);
            }
        }
    }

    @Override
    public List<AuctionCategory> getCategoryTree(boolean includeDisabled) {
        QueryWrapper<AuctionCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        if (!includeDisabled) {
            wrapper.eq("category_status", 1); // 默认只获取启用的分类
        }
        wrapper.orderByAsc("category_sort", "id");
        List<AuctionCategory> allCategories = list(wrapper);
        
        // 构建树形结构
        return buildCategoryTree(allCategories);
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

}
