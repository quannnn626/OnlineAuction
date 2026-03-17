package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionDepositRechargeApply;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositRechargeApplyService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 保证金充值申请：用户申请，财务审核
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionDepositRechargeApply")
public class AuctionDepositRechargeApplyController {

    @Autowired
    private IAuctionDepositRechargeApplyService rechargeApplyService;

    /** 用户：我的充值申请列表 */
    @GetMapping("/my/page")
    public Result<PageInfo<AuctionDepositRechargeApply>> getMyPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) return Result.error("未登录");
            PageInfo<AuctionDepositRechargeApply> page = rechargeApplyService.getMyPage(current, size, userId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 用户：申请充值 */
    @PostMapping("/apply")
    public Result<Long> apply(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) return Result.error("未登录");
            Object amt = body.get("amount");
            BigDecimal amount = amt != null ? (amt instanceof Number ? BigDecimal.valueOf(((Number) amt).doubleValue()) : new BigDecimal(amt.toString())) : null;
            String applyRemark = body.get("applyRemark") != null ? body.get("applyRemark").toString() : null;
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("充值金额必须大于0");
            }
            Long id = rechargeApplyService.apply(userId, amount, applyRemark);
            return Result.success("申请成功，请等待财务审核", id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 财务：充值申请列表 */
    @GetMapping("/admin/page")
    public Result<PageInfo<AuctionDepositRechargeApply>> getAdminPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看");
            }
            PageInfo<AuctionDepositRechargeApply> page = rechargeApplyService.getAdminPage(current, size, status);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 财务：通过充值申请（并执行充值） */
    @PostMapping("/admin/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限操作");
            }
            Long handleUserId = getUserId(request);
            String handleRemark = body != null && body.get("handleRemark") != null ? body.get("handleRemark").toString() : null;
            rechargeApplyService.approve(id, handleUserId, handleRemark);
            return Result.success("已通过并完成充值", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 财务：驳回充值申请 */
    @PostMapping("/admin/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限操作");
            }
            Long handleUserId = getUserId(request);
            String handleRemark = body != null && body.get("handleRemark") != null ? body.get("handleRemark").toString() : null;
            rechargeApplyService.reject(id, handleUserId, handleRemark);
            return Result.success("已驳回", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object v = session.getAttribute("userId");
        if (v instanceof Long) return (Long) v;
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }
}
