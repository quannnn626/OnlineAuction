package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.entity.AuctionInvoice;
import com.auction.onlineauction.OnlineAuction.service.IAuctionInvoiceService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 发票管理：用户申请/查看，财务查看/上传/驳回
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionInvoice")
public class AuctionInvoiceController {

    @Autowired
    private IAuctionInvoiceService invoiceService;

    /** 用户：我的发票申请列表 */
    @GetMapping("/my/page")
    public Result<PageInfo<AuctionInvoice>> getMyPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) return Result.error("未登录");
            PageInfo<AuctionInvoice> page = invoiceService.getMyPage(current, size, userId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 用户：申请发票 */
    @PostMapping("/apply")
    public Result<Long> apply(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) return Result.error("未登录");
            Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
            String invoiceTitle = body.get("invoiceTitle") != null ? body.get("invoiceTitle").toString().trim() : null;
            String taxNo = body.get("taxNo") != null ? body.get("taxNo").toString().trim() : null;
            String mailAddress = body.get("mailAddress") != null ? body.get("mailAddress").toString().trim() : null;
            Object amt = body.get("amount");
            BigDecimal amount = amt != null ? (amt instanceof Number ? BigDecimal.valueOf(((Number) amt).doubleValue()) : new BigDecimal(amt.toString())) : null;
            Integer invoiceType = body.get("invoiceType") != null ? Integer.valueOf(body.get("invoiceType").toString()) : 1;
            if (invoiceTitle == null || invoiceTitle.isEmpty() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("发票抬头和金额不能为空");
            }
            Long id = invoiceService.apply(userId, orderId, invoiceTitle, taxNo, mailAddress, amount, invoiceType);
            return Result.success("申请成功", id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 财务：发票申请列表 */
    @GetMapping("/admin/page")
    public Result<PageInfo<AuctionInvoice>> getAdminPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看");
            }
            PageInfo<AuctionInvoice> page = invoiceService.getAdminPage(current, size, status, userId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 用户：获取本人某条发票的发票文件信息（用于下载/查看），仅已开票且有文件时返回 */
    @GetMapping("/my/{id}/file")
    public Result<AuctionFile> getMyInvoiceFile(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) return Result.error("未登录");
            AuctionFile file = invoiceService.getMyInvoiceFile(id, userId);
            if (file == null) return Result.error("无发票文件或无权查看");
            return Result.success("查询成功", file);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /** 财务：处理发票（上传开票文件或驳回） */
    @PutMapping("/admin/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限操作");
            }
            Long handleUserId = getUserId(request);
            Integer status = body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null;
            Long fileId = body.get("fileId") != null ? Long.valueOf(body.get("fileId").toString()) : null;
            String handleRemark = body.get("handleRemark") != null ? body.get("handleRemark").toString() : null;
            invoiceService.handleInvoice(id, status, fileId, handleRemark, handleUserId);
            return Result.success("处理成功", null);
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
