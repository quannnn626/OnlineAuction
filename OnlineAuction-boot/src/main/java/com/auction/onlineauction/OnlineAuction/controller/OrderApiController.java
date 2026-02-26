package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 订单 API（用户端，查看自己的订单）
 */
@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Autowired
    private IAuctionOrderService orderService;

    @GetMapping("/page")
    public Result<PageInfo<AuctionOrder>> getMyPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(defaultValue = "buyer") String role,
            HttpServletRequest request) {
        try {
            if (request.getSession(false) == null) return Result.error("请先登录");
            Long userId = (Long) request.getSession(false).getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            Long buyerId = "buyer".equalsIgnoreCase(role) ? userId : null;
            Long sellerId = "seller".equalsIgnoreCase(role) ? userId : null;
            PageInfo<AuctionOrder> page = orderService.getOrderPage(current, size, orderStatus, buyerId, sellerId, null);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<AuctionOrder> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (request.getSession(false) == null) return Result.error("请先登录");
            Long userId = (Long) request.getSession(false).getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            AuctionOrder order = orderService.getById(id);
            if (order == null || order.getDelFlag() == 1) {
                return Result.error("订单不存在");
            }
            if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
                return Result.error("无权限查看该订单");
            }
            return Result.success("查询成功", order);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
