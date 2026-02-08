package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

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
            PageInfo<AuctionGoods> pageInfo = goodsService.getGoodsPage(current, size, goodsName, categoryId, auditStatus, goodsStatus);
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
            List<AuctionGoods> list = goodsService.getGoodsList();
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
            AuctionGoods goods = goodsService.getGoodsById(id);
            return Result.success("查询成功", goods);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增商品
     */
    @PostMapping("/addGoods")
    public Result<AuctionGoods> addGoods(@RequestBody Map<String, Object> requestData) {
        try {
            AuctionGoods goods = goodsService.addGoods(requestData);
            return Result.success("新增成功", goods);
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<AuctionGoods> updateGoods(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            AuctionGoods goods = goodsService.updateGoods(id, requestData);
            return Result.success("更新成功", goods);
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
            goodsService.deleteGoods(id);
            return Result.success("删除成功", null);
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
            goodsService.batchDeleteGoods(ids);
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
            goodsService.auditGoods(id, auditStatus, auditRemark);
            return Result.success("审核成功", null);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 重新申请上架（卖家操作）
     */
    @PutMapping("/reapply/{id}")
    public Result<Void> reapplyGoods(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录");
            }
            goodsService.reapplyGoods(id, userId);
            return Result.success("重新申请上架成功，等待管理员审核", null);
        } catch (Exception e) {
            return Result.error("重新申请上架失败：" + e.getMessage());
        }
    }
}
