package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.service.IAuctionRecordService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 竞拍记录表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionRecord")
public class AuctionRecordController {

    @Autowired
    private IAuctionRecordService recordService;

    /**
     * 提交竞拍出价
     */
    @PostMapping("/bid")
    public Result<AuctionRecord> submitBid(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }

            Long buyerId = (Long) session.getAttribute("userId");
            if (buyerId == null) {
                return Result.error("未登录");
            }

            Long goodsId = Long.valueOf(params.get("goodsId").toString());
            BigDecimal bidPrice = new BigDecimal(params.get("bidPrice").toString());

            AuctionRecord record = recordService.submitBid(goodsId, buyerId, bidPrice);
            return Result.success("出价成功", record);
        } catch (Exception e) {
            return Result.error("出价失败：" + e.getMessage());
        }
    }

    /**
     * 查询商品的竞拍记录列表
     */
    @GetMapping("/list")
    public Result<List<AuctionRecord>> getRecordsByGoodsId(
            @RequestParam Long goodsId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<AuctionRecord> records = recordService.getRecordsByGoodsId(goodsId, limit);
            return Result.success("查询成功", records);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 历史竞拍管理（用户端）：只看自己参与过竞拍的商品（按商品聚合）
     */
    @GetMapping("/my-goods/page")
    public Result<PageInfo<Map<String, Object>>> getMyBidGoodsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录");
            }
            PageInfo<Map<String, Object>> page = recordService.getMyBidGoodsPage(current, size, userId, keyword);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 历史竞拍（用户端）：查看自己在某商品下的竞拍记录
     */
    @GetMapping("/my-goods/{goodsId}/records/page")
    public Result<PageInfo<AuctionRecord>> getMyBidRecordsByGoodsPage(
            @PathVariable Long goodsId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录");
            }
            PageInfo<AuctionRecord> page = recordService.getMyBidRecordsByGoodsPage(current, size, goodsId, userId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 历史竞拍管理（后台）：管理员查看所有有竞拍记录的商品（按商品聚合）
     */
    @GetMapping("/admin/goods/page")
    public Result<PageInfo<Map<String, Object>>> getAdminBidGoodsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (!RoleCheckHelper.canViewAuctionHistoryAdmin(session)) {
                return Result.error("无权限查看历史竞拍管理");
            }
            PageInfo<Map<String, Object>> page = recordService.getAdminBidGoodsPage(current, size, keyword);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 历史竞拍管理详情（后台）：点击某商品后查看该商品竞拍记录明细
     */
    @GetMapping("/admin/goods/{goodsId}/records/page")
    public Result<PageInfo<AuctionRecord>> getAdminBidRecordsByGoodsPage(
            @PathVariable Long goodsId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (!RoleCheckHelper.canViewAuctionHistoryAdmin(session)) {
                return Result.error("无权限查看历史竞拍详情");
            }
            PageInfo<AuctionRecord> page = recordService.getBidRecordsByGoodsPage(current, size, goodsId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 按买方分页查询竞拍记录（含商品名，用于保证金管理） */
    @GetMapping("/admin/buyer/{buyerId}/records/page")
    public Result<PageInfo<AuctionRecord>> getRecordsByBuyerIdPage(
            @PathVariable Long buyerId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看");
            }
            PageInfo<AuctionRecord> page = recordService.getRecordsByBuyerIdPage(current, size, buyerId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 拍卖师/管理员标记异常出价：0=正常 1=恶意出价 2=机器人
     */
    @PutMapping("/{id}/abnormal")
    public Result<Void> markAbnormal(@PathVariable Long id, @RequestParam Integer type, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAuctioneerManage(request.getSession(false))) {
                return Result.error("无权限，仅拍卖师/管理员可标记");
            }
            recordService.markAbnormal(id, type);
            return Result.success("已更新异常标记", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
