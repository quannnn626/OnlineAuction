package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
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
@RequestMapping("/api/OnlineAuction/auctionDeposit")
public class AuctionDepositController {

    @Autowired
    private IAuctionDepositService depositService;

    @GetMapping("/page")
    public Result<PageInfo<AuctionDeposit>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer depositType,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看保证金");
            }
            PageInfo<AuctionDeposit> page = depositService.getDepositPage(current, size, userId, depositType);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/balance/{userId}")
    public Result<Map<String, Object>> getBalance(@PathVariable Long userId, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看保证金");
            }
            BigDecimal balance = depositService.getBalanceByUserId(userId);
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("balance", balance);
            return Result.success("查询成功", map);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/topUp")
    public Result<Void> manualTopUp(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限充值保证金");
            }
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            Object amt = body.get("amount");
            BigDecimal amount = amt instanceof Number ? BigDecimal.valueOf(((Number) amt).doubleValue()) : new BigDecimal(amt.toString());
            String remark = body.get("remark") != null ? body.get("remark").toString() : null;
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            depositService.manualTopUp(userId, amount, remark);
            return Result.success("充值成功", null);
        } catch (Exception e) {
            return Result.error("充值失败：" + e.getMessage());
        }
    }
}
