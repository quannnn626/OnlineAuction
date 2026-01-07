package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 拍卖商品表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionGoods")
public class AuctionGoodsController {

    @Autowired
    private IAuctionGoodsService goodsService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionGoods>> getGoodsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String goodsName,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer goodsStatus) {
        try {
            // 使用 PageHelper 开始分页
            PageHelper.startPage(current, size);

            QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (goodsName != null && !goodsName.trim().isEmpty()) {
                wrapper.like("goods_name", goodsName);
            }
            if (categoryId != null) {
                wrapper.eq("category_id", categoryId);
            }
            if (auditStatus != null) {
                wrapper.eq("audit_status", auditStatus);
            }
            if (goodsStatus != null) {
                wrapper.eq("goods_status", goodsStatus);
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
     * 获取所有商品列表（不分页，用于下拉选择等）
     */
    @GetMapping("/list")
    public Result<List<AuctionGoods>> getGoodsList() {
        try {
            QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            wrapper.orderByDesc("create_time");
            List<AuctionGoods> list = goodsService.list(wrapper);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情
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
     * 新增商品
     */
    @PostMapping("/addGoods")
    public Result<AuctionGoods> addGoods(@RequestBody AuctionGoods goods) {
        try {
            // 验证商品名称
            if (goods.getGoodsName() == null || goods.getGoodsName().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            // 设置默认值
            if (goods.getAuditStatus() == null) {
                goods.setAuditStatus(0); // 待审核
            }
            if (goods.getGoodsStatus() == null) {
                goods.setGoodsStatus(0); // 未开始
            }
            goods.setSellerId(1L);
            goods.setCreateTime(LocalDateTime.now());
            goods.setUpdateTime(LocalDateTime.now());
            goods.setDelFlag(0);
            boolean success = goodsService.save(goods);
            if (success) {
                return Result.success("新增成功", goods);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<AuctionGoods> updateGoods(@PathVariable Long id, @RequestBody AuctionGoods goods) {
        try {
            // 验证商品是否存在
            AuctionGoods existing = goodsService.getById(id);
            if (existing == null || existing.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            // 验证商品名称
            if (goods.getGoodsName() == null || goods.getGoodsName().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            // 设置更新信息
            goods.setId(id);
            goods.setUpdateTime(LocalDateTime.now());
            // 保留创建时间
            goods.setCreateTime(existing.getCreateTime());
            // 保留删除标志
            goods.setDelFlag(existing.getDelFlag());
            boolean success = goodsService.updateById(goods);
            if (success) {
                return Result.success("更新成功", goods);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
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
            goods.setUpdateTime(LocalDateTime.now());
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

    /**
     * 批量删除商品
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteGoods(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的商品");
            }
            for (Long id : ids) {
                AuctionGoods goods = goodsService.getById(id);
                if (goods != null && goods.getDelFlag() == 0) {
                    goods.setDelFlag(1);
                    goods.setUpdateTime(LocalDateTime.now());
                    goodsService.updateById(goods);
                }
            }
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 审核商品
     */
    @PutMapping("/audit/{id}")
    public Result<Void> auditGoods(@PathVariable Long id, @RequestParam Integer auditStatus, @RequestParam(required = false) String auditRemark) {
        try {
            AuctionGoods goods = goodsService.getById(id);
            if (goods == null || goods.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            goods.setAuditStatus(auditStatus);
            if (auditRemark != null) {
                goods.setAuditRemark(auditRemark);
            }
            goods.setUpdateTime(LocalDateTime.now());
            boolean success = goodsService.updateById(goods);
            if (success) {
                return Result.success("审核成功", null);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}
