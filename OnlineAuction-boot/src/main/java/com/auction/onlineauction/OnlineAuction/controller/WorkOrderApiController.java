package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionWorkOrder;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOperLog;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOperLogService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionWorkOrderService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderApiController {

    @Autowired
    private IAuctionWorkOrderService workOrderService;

    @Autowired
    private IAuctionOperLogService operLogService;

    @PostMapping
    public Result<AuctionWorkOrder> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.hasAnyRole(session, 1, 2)) return Result.error("仅买方/卖方可提交工单");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            String workType = body.get("workType") == null ? "other" : body.get("workType").toString();
            Long relatedId = body.get("relatedId") == null ? null : Long.valueOf(body.get("relatedId").toString());
            String title = body.get("title") == null ? null : body.get("title").toString();
            String content = body.get("content") == null ? null : body.get("content").toString();
            AuctionWorkOrder row = workOrderService.createWorkOrder(userId, workType, relatedId, title, content);
            return Result.success("提交成功", row);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<PageInfo<Map<String, Object>>> my(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.hasAnyRole(session, 1, 2)) return Result.error("无权限查看");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            return Result.success("查询成功", workOrderService.getMyWorkOrders(userId, current, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/service")
    public Result<PageInfo<Map<String, Object>>> service(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer workStatus,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.isCustomerService(session)) return Result.error("仅客服可查看");
            Long serviceId = (Long) session.getAttribute("userId");
            if (serviceId == null) return Result.error("请先登录");
            return Result.success("查询成功", workOrderService.getServiceWorkOrders(serviceId, current, size, workStatus));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/service-process")
    public Result<Void> serviceProcess(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.isCustomerService(session)) return Result.error("仅客服可处理");
            Long serviceId = (Long) session.getAttribute("userId");
            if (serviceId == null) return Result.error("请先登录");
            Integer workStatus = body.get("workStatus") == null ? null : Integer.valueOf(body.get("workStatus").toString());
            String handleResult = body.get("handleResult") == null ? null : body.get("handleResult").toString();
            String penaltyType = body.get("penaltyType") == null ? null : body.get("penaltyType").toString();
            Long penaltyTargetUserId = body.get("penaltyTargetUserId") == null ? null : Long.valueOf(body.get("penaltyTargetUserId").toString());
            BigDecimal penaltyAmount = body.get("penaltyAmount") == null ? null : new BigDecimal(body.get("penaltyAmount").toString());
            workOrderService.serviceProcessWorkOrder(
                    id, serviceId, workStatus, handleResult, penaltyType, penaltyTargetUserId, penaltyAmount
            );
            return Result.success("处理成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin-review")
    public Result<PageInfo<Map<String, Object>>> adminReviewPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer workStatus,
            @RequestParam(required = false) String workType,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.hasAnyRole(session, 3)) return Result.error("仅管理员可复核");
            return Result.success("查询成功", workOrderService.getAdminReviewWorkOrders(current, size, workStatus, workType));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/admin-review")
    public Result<Void> adminReview(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.hasAnyRole(session, 3)) return Result.error("仅管理员可复核");
            Long adminId = (Long) session.getAttribute("userId");
            if (adminId == null) return Result.error("请先登录");
            Boolean approve = body.get("approve") == null ? null : Boolean.valueOf(body.get("approve").toString());
            String reviewResult = body.get("reviewResult") == null ? null : body.get("reviewResult").toString();
            String penaltyType = body.get("penaltyType") == null ? null : body.get("penaltyType").toString();
            Long penaltyTargetUserId = body.get("penaltyTargetUserId") == null ? null : Long.valueOf(body.get("penaltyTargetUserId").toString());
            BigDecimal penaltyAmount = body.get("penaltyAmount") == null ? null : new BigDecimal(body.get("penaltyAmount").toString());

            AuctionWorkOrder before = workOrderService.getById(id);
            String workType = before != null ? before.getWorkType() : null;

            workOrderService.adminReviewWorkOrder(
                    id, adminId, approve, reviewResult, penaltyType, penaltyTargetUserId, penaltyAmount
            );

            // 风控工单复核：写入操作审计
            if ("risk".equalsIgnoreCase(workType)) {
                try {
                    AuctionOperLog log = new AuctionOperLog();
                    log.setOperUserId(adminId);
                    log.setOperModule("risk-work-order");
                    log.setOperType(approve != null && approve ? "approve" : "reject");
                    log.setOperContent("workOrderId=" + id + ", penaltyType=" + penaltyType + ", targetUserId=" + penaltyTargetUserId);
                    log.setOperIp(getClientIp(request));
                    log.setCreateTime(LocalDateTime.now());
                    operLogService.save(log);
                } catch (Exception ignored) {
                }
            }
            return Result.success("复核完成", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}
