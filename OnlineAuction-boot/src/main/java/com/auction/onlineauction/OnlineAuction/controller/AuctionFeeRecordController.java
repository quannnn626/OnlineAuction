package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionFeeRecord;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFeeRecordService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionSystemConfigService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 佣金结算、平台扣费记录（财务查看）
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionFeeRecord")
public class AuctionFeeRecordController {

    @Autowired
    private IAuctionFeeRecordService feeRecordService;
    @Autowired
    private IAuctionOrderService orderService;
    @Autowired
    private IAuctionSystemConfigService systemConfigService;

    @GetMapping("/page")
    public Result<PageInfo<AuctionFeeRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer feeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看");
            }
            PageInfo<AuctionFeeRecord> page = feeRecordService.getPage(current, size, feeType, status, userId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 获取佣金比例 */
    @GetMapping("/commissionRate")
    public Result<Map<String, Object>> getCommissionRate(HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限");
            }
            BigDecimal rate = systemConfigService.getCommissionRate();
            Map<String, Object> map = new HashMap<>();
            map.put("commissionRate", rate);
            map.put("commissionRatePercent", rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString());
            return Result.success("查询成功", map);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 设置佣金比例（0～1，如 0.05 表示 5%） */
    @PutMapping("/commissionRate")
    public Result<Void> setCommissionRate(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限");
            }
            Object v = body.get("commissionRate");
            BigDecimal rate = v instanceof Number ? BigDecimal.valueOf(((Number) v).doubleValue()) : new BigDecimal(v.toString());
            systemConfigService.setCommissionRate(rate);
            return Result.success("设置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 待佣金结算的订单（已完成且未结算） */
    @GetMapping("/ordersPendingCommission")
    public Result<PageInfo<Map<String, Object>>> getOrdersPendingCommission(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限");
            }
            PageInfo<Map<String, Object>> page = orderService.getOrdersPendingCommissionSettlement(current, size);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 佣金结算确认：对指定订单计算佣金并计入平台收入 */
    @PostMapping("/confirmSettle/{orderId}")
    public Result<Void> confirmSettle(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限");
            }
            BigDecimal overrideAmount = null;
            if (body != null && body.get("overrideAmount") != null) {
                Object o = body.get("overrideAmount");
                overrideAmount = o instanceof Number ? BigDecimal.valueOf(((Number) o).doubleValue()) : new BigDecimal(o.toString());
            }
            BigDecimal rate = systemConfigService.getCommissionRate();
            feeRecordService.confirmCommissionSettle(orderId, rate, overrideAmount);
            return Result.success("结算成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 导出扣费记录（返回列表，前端可转 CSV/Excel 对账） */
    @GetMapping("/export")
    public Result<List<AuctionFeeRecord>> export(
            @RequestParam(required = false) Integer feeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限");
            }
            List<AuctionFeeRecord> list = feeRecordService.listForExport(feeType, status, userId, startTime, endTime);
            return Result.success("导出成功", list);
        } catch (Exception e) {
            return Result.error("导出失败：" + e.getMessage());
        }
    }
}
