package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 竞拍订单（后台）
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
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            PageInfo<AuctionOrder> page;
            if (RoleCheckHelper.canViewOrderAdmin(session)) {
                page = orderService.getOrderPage(current, size, orderStatus, buyerId, sellerId, orderNo);
            } else if (RoleCheckHelper.isCustomerService(session)) {
                Long serviceId = (Long) session.getAttribute("userId");
                if (serviceId == null) {
                    return Result.error("未登录");
                }
                page = orderService.getOrderPageForConsultedUsers(serviceId, current, size, orderStatus, orderNo != null ? orderNo : "");
            } else {
                return Result.error("无权限查看订单");
            }
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<AuctionOrder> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            AuctionOrder order = orderService.getById(id);
            if (order == null || order.getDelFlag() == 1) {
                return Result.error("订单不存在");
            }
            if (RoleCheckHelper.canViewOrderAdmin(session)) {
                return Result.success("查询成功", order);
            }
            if (RoleCheckHelper.isCustomerService(session)) {
                Long serviceId = (Long) session.getAttribute("userId");
                if (serviceId != null && orderService.canServiceViewOrder(id, serviceId)) {
                    return Result.success("查询成功", order);
                }
            }
            return Result.error("无权限查看订单");
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

    /** 落槌确认：拍卖师/管理员确认成交，生成成交确认书编号 */
    @PostMapping("/{id}/confirm-deal")
    public Result<Void> confirmDeal(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canConfirmDeal(request.getSession(false))) {
                return Result.error("无权限落槌确认");
            }
            Long uid = (Long) request.getSession(false).getAttribute("userId");
            if (uid == null) return Result.error("未登录");
            orderService.confirmDeal(id, uid);
            return Result.success("落槌确认成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 发货：填写快递信息 */
    @PutMapping("/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            AuctionOrder order = orderService.getById(id);
            if (order == null) return Result.error("订单不存在");
            if (!RoleCheckHelper.canShipOrder(request.getSession(false), order.getSellerId())) {
                return Result.error("无权限发货");
            }
            String expressCompany = body != null && body.get("expressCompany") != null ? body.get("expressCompany").toString() : null;
            String expressNo = body != null && body.get("expressNo") != null ? body.get("expressNo").toString() : null;
            orderService.shipOrder(id, expressCompany, expressNo);
            return Result.success("发货成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
