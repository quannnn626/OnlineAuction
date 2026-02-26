package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 竞拍订单管理（后台）
 * 财务、管理员、超级管理员可操作
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionOrder")
public class AuctionOrderController {

    @Autowired
    private IAuctionOrderService orderService;

    @GetMapping("/page")
    public Result<PageInfo<AuctionOrder>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String orderNo,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageOrderAdmin(request.getSession(false))) {
                return Result.error("无权限查看订单");
            }
            PageInfo<AuctionOrder> page = orderService.getOrderPage(current, size, orderStatus, buyerId, sellerId, orderNo);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<AuctionOrder> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageOrderAdmin(request.getSession(false))) {
                return Result.error("无权限查看订单");
            }
            AuctionOrder order = orderService.getById(id);
            if (order == null || order.getDelFlag() == 1) {
                return Result.error("订单不存在");
            }
            return Result.success("查询成功", order);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageOrderAdmin(request.getSession(false))) {
                return Result.error("无权限更新订单状态");
            }
            Integer newStatus = body.get("orderStatus") != null ? Integer.valueOf(body.get("orderStatus").toString()) : null;
            if (newStatus == null) {
                return Result.error("订单状态不能为空");
            }
            orderService.updateOrderStatus(id, newStatus);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/refund")
    public Result<Void> processRefund(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body,
                                     HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canProcessRefund(request.getSession(false))) {
                return Result.error("无权限处理退款");
            }
            String remark = body != null && body.get("remark") != null ? body.get("remark").toString() : null;
            orderService.processRefund(id, remark);
            return Result.success("退款成功", null);
        } catch (Exception e) {
            return Result.error("退款失败：" + e.getMessage());
        }
    }
}
