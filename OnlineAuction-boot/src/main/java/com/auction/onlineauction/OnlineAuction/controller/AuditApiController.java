package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOperLog;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOperLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditApiController {

    @Autowired
    private IAuctionOperLogService operLogService;

    private boolean canAudit(HttpSession session) {
        return RoleCheckHelper.canViewAuditLogs(session);
    }

    @GetMapping("/oper-logs/page")
    public Result<PageInfo<AuctionOperLog>> operLogsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long operUserId,
            @RequestParam(required = false) String operModule,
            @RequestParam(required = false) String operType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !canAudit(session)) return Result.error("无权限");
            PageHelper.startPage(current, size);
            QueryWrapper<AuctionOperLog> q = buildCommonQuery(operUserId, operModule, operType, keyword, startTime, endTime);
            q.orderByDesc("create_time").orderByDesc("id");
            List<AuctionOperLog> list = operLogService.list(q);
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/login-logs/page")
    public Result<PageInfo<AuctionOperLog>> loginLogsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long operUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !canAudit(session)) return Result.error("无权限");
            PageHelper.startPage(current, size);
            QueryWrapper<AuctionOperLog> q = buildCommonQuery(operUserId, "auth", null, keyword, startTime, endTime);
            q.in("oper_type", Arrays.asList("login", "login_fail"));
            q.orderByDesc("create_time").orderByDesc("id");
            List<AuctionOperLog> list = operLogService.list(q);
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/sensitive-logs/page")
    public Result<PageInfo<AuctionOperLog>> sensitiveLogsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long operUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !canAudit(session)) return Result.error("无权限");
            PageHelper.startPage(current, size);
            QueryWrapper<AuctionOperLog> q = buildCommonQuery(operUserId, null, null, keyword, startTime, endTime);
            q.and(w -> w.in("oper_module", Arrays.asList("risk", "risk-work-order"))
                    .or().in("oper_type", Arrays.asList(
                            "risk_apply", "risk_apply_batch", "risk_level", "risk_level_batch",
                            "approve", "reject"
                    ))
                    .or().like("oper_content", "冻结")
                    .or().like("oper_content", "解封")
                    .or().like("oper_content", "退款")
                    .or().like("oper_content", "删除"));
            q.orderByDesc("create_time").orderByDesc("id");
            List<AuctionOperLog> list = operLogService.list(q);
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private QueryWrapper<AuctionOperLog> buildCommonQuery(
            Long operUserId,
            String operModule,
            String operType,
            String keyword,
            String startTime,
            String endTime
    ) {
        QueryWrapper<AuctionOperLog> q = new QueryWrapper<>();
        if (operUserId != null) q.eq("oper_user_id", operUserId);
        if (operModule != null && !operModule.trim().isEmpty()) q.eq("oper_module", operModule.trim());
        if (operType != null && !operType.trim().isEmpty()) q.eq("oper_type", operType.trim());
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            q.and(w -> w.like("oper_type", kw).or().like("oper_content", kw).or().like("oper_module", kw));
        }
        if (startTime != null && !startTime.trim().isEmpty()) q.ge("create_time", startTime.trim());
        if (endTime != null && !endTime.trim().isEmpty()) q.le("create_time", endTime.trim());
        return q;
    }
}
