package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/OnlineAuction/auctionDeposit")
public class AuctionDepositController {

    @Autowired
    private IAuctionDepositService depositService;
    @Autowired
    private IAuctionUserService userService;

    /** 按用户名/昵称搜索用户（用于冻结/解冻/充值时的选择器） */
    @GetMapping("/searchUsers")
    public Result<List<Map<String, Object>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限");
            }
            List<Map<String, Object>> list = userService.searchUsersForSelection(keyword, limit);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 平台保证金汇总（总额、可用、冻结、用户数） */
    @GetMapping("/platformSummary")
    public Result<Map<String, Object>> getPlatformSummary(HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看保证金");
            }
            return Result.success("查询成功", depositService.getPlatformSummary());
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 用户保证金汇总分页（按用户名/昵称搜索） */
    @GetMapping("/summaryPage")
    public Result<PageInfo<Map<String, Object>>> getSummaryPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userName,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看保证金");
            }
            PageInfo<Map<String, Object>> page = depositService.getDepositSummaryPage(current, size, userName);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

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

    /** 财务冻结用户保证金 */
    @PostMapping("/freeze")
    public Result<Void> freeze(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限操作");
            }
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            Object amt = body.get("amount");
            BigDecimal amount = amt != null ? (amt instanceof Number ? BigDecimal.valueOf(((Number) amt).doubleValue()) : new BigDecimal(amt.toString())) : null;
            String remark = body.get("remark") != null ? body.get("remark").toString() : null;
            if (userId == null || amount == null) {
                return Result.error("用户ID和金额不能为空");
            }
            depositService.freezeByFinance(userId, amount, remark);
            return Result.success("冻结成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 财务解冻用户保证金 */
    @PostMapping("/unfreeze")
    public Result<Void> unfreeze(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限操作");
            }
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            Object amt = body.get("amount");
            BigDecimal amount = amt != null ? (amt instanceof Number ? BigDecimal.valueOf(((Number) amt).doubleValue()) : new BigDecimal(amt.toString())) : null;
            String remark = body.get("remark") != null ? body.get("remark").toString() : null;
            if (userId == null || amount == null) {
                return Result.error("用户ID和金额不能为空");
            }
            depositService.unfreezeByFinance(userId, amount, remark);
            return Result.success("解冻成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
