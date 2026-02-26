package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/deposit")
public class DepositApiController {

    @Autowired
    private IAuctionDepositService depositService;

    @GetMapping("/balance")
    public Result<Map<String, Object>> getMyBalance(HttpServletRequest request) {
        try {
            if (request.getSession(false) == null) return Result.error("请先登录");
            Long userId = (Long) request.getSession(false).getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            BigDecimal balance = depositService.getBalanceByUserId(userId);
            Map<String, Object> map = new HashMap<>();
            map.put("balance", balance);
            return Result.success("查询成功", map);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/page")
    public Result<PageInfo<AuctionDeposit>> getMyPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            if (request.getSession(false) == null) return Result.error("请先登录");
            Long userId = (Long) request.getSession(false).getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            PageInfo<AuctionDeposit> page = depositService.getDepositPage(current, size, userId, null);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
