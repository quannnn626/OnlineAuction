package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOperLog;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.entity.AuctionWorkOrder;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionRecordMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOperLogService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionWorkOrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
public class RiskApiController {

    @Autowired
    private IAuctionUserService userService;

    @Autowired
    private IAuctionWorkOrderService workOrderService;

    @Autowired
    private IAuctionOperLogService operLogService;

    @Autowired
    private AuctionRecordMapper recordMapper;

    private boolean isRisk(HttpSession session) {
        return RoleCheckHelper.isRiskControl(session);
    }

    @PostMapping("/users/{id}/risk-level")
    public Result<Void> updateRiskLevel(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");

            Integer riskLevel = body.get("riskLevel") == null ? null : Integer.valueOf(body.get("riskLevel").toString());
            if (riskLevel == null) return Result.error("riskLevel不能为空");
            if (riskLevel < 0) riskLevel = 0;
            if (riskLevel > 3) riskLevel = 3;

            AuctionUser target = userService.getById(id);
            if (target == null || target.getDelFlag() == 1) return Result.error("目标用户不存在");

            target.setRiskLevel(riskLevel);
            userService.updateById(target);

            insertOperLog(session, request, "risk", "risk_level", "目标用户ID=" + id + ", riskLevel=" + riskLevel);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/user-status/apply")
    public Result<AuctionWorkOrder> applyUserStatus(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");

            Long riskOfficerId = (Long) session.getAttribute("userId");
            if (riskOfficerId == null) return Result.error("请先登录");

            Long targetUserId = body.get("targetUserId") == null ? null : Long.valueOf(body.get("targetUserId").toString());
            String actionType = body.get("actionType") == null ? null : body.get("actionType").toString();
            String remark = body.get("remark") == null ? null : body.get("remark").toString();

            AuctionWorkOrder w = workOrderService.createRiskActionWorkOrder(riskOfficerId, actionType, targetUserId, remark);
            insertOperLog(session, request, "risk", "risk_apply", "actionType=" + actionType + ", targetUserId=" + targetUserId);
            return Result.success("申请已提交，等待管理员复核", w);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public Result<AuctionUser> getUserRisk(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");
            AuctionUser u = userService.getById(id);
            if (u == null || u.getDelFlag() == 1) return Result.error("用户不存在");
            // 不向前端返回密码
            u.setPassword(null);
            return Result.success("查询成功", u);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 有风险行为的用户：曾触发风控出价规则（risk_rule_type 大于 0）或当前风险等级大于 0
     */
    @GetMapping("/users/risk-activity/page")
    public Result<PageInfo<Map<String, Object>>> riskActivityUsersPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");
            PageHelper.startPage(current, size);
            List<Map<String, Object>> list = userService.listUsersWithRiskActivityForRisk();
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/users/risk-level/batch")
    public Result<Void> updateRiskLevelBatch(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");

            List<Long> userIds = parseUserIds(body.get("userIds"));
            if (userIds.isEmpty()) return Result.error("请选择用户");

            Integer riskLevel = body.get("riskLevel") == null ? null : Integer.valueOf(body.get("riskLevel").toString());
            if (riskLevel == null) return Result.error("风险等级不能为空");

            userService.batchUpdateRiskLevelForRisk(userIds, riskLevel);
            insertOperLog(session, request, "risk", "risk_level_batch", "userIds=" + userIds + ", riskLevel=" + riskLevel);
            return Result.success("已更新选中用户的风险等级", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/user-status/apply-batch")
    public Result<Map<String, Object>> applyUserStatusBatch(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");

            Long riskOfficerId = (Long) session.getAttribute("userId");
            if (riskOfficerId == null) return Result.error("请先登录");

            List<Long> userIds = parseUserIds(body.get("userIds"));
            if (userIds.isEmpty()) return Result.error("请选择用户");

            String actionType = body.get("actionType") == null ? null : body.get("actionType").toString();
            String remark = body.get("remark") == null ? null : body.get("remark").toString();

            List<AuctionWorkOrder> orders = workOrderService.createRiskActionWorkOrdersBatch(riskOfficerId, actionType, userIds, remark);
            insertOperLog(session, request, "risk", "risk_apply_batch", "actionType=" + actionType + ", count=" + orders.size());

            Map<String, Object> data = new HashMap<>();
            data.put("count", orders.size());
            return Result.success("已提交 " + orders.size() + " 条申请，等待管理员复核", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private static List<Long> parseUserIds(Object raw) {
        if (!(raw instanceof List)) {
            return new ArrayList<>();
        }
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        for (Object o : (List<?>) raw) {
            if (o == null) continue;
            try {
                set.add(Long.valueOf(o.toString()));
            } catch (NumberFormatException ignored) {
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 异常出价监控：列出异常出价记录。
     * abnormalType 不传或为空：查看全部异常类型（risk_rule_type &gt; 0）；传入 1~5 则按规则筛选。
     */
    @GetMapping("/abnormal-bids/page")
    public Result<PageInfo<AuctionRecord>> abnormalBidsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long goodsId,
            @RequestParam(required = false) Integer abnormalType,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");
            PageHelper.startPage(current, size);
            List<AuctionRecord> list = recordMapper.selectAbnormalBidRecordsForRisk(goodsId, abnormalType);
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 恶意参拍：按买家汇总异常出价次数（abnormal_type=1）
     */
    @GetMapping("/malicious-bidders/page")
    public Result<PageInfo<Map<String, Object>>> maliciousBiddersPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "60") Integer windowMinutes,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");
            PageHelper.startPage(current, size);
            List<Map<String, Object>> list = recordMapper.selectMaliciousBiddersForRisk(windowMinutes);
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 围标/刷价（疑似）：在窗口期内同一用户异常/高频出价次数达到阈值
     */
    @GetMapping("/ring-bidders/page")
    public Result<PageInfo<Map<String, Object>>> ringBiddersPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "30") Integer windowMinutes,
            @RequestParam(defaultValue = "5") Integer minBidCount,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");
            PageHelper.startPage(current, size);
            List<Map<String, Object>> list = recordMapper.selectSuspectedRingBiddersForRisk(windowMinutes, minBidCount);
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 操作日志分页查询（auction_oper_log）
     */
    @GetMapping("/oper-logs/page")
    public Result<PageInfo<AuctionOperLog>> operLogsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String operModule,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !isRisk(session)) return Result.error("无权限");

            PageHelper.startPage(current, size);
            QueryWrapper<AuctionOperLog> q = new QueryWrapper<>();
            if (operModule != null && !operModule.trim().isEmpty()) {
                q.eq("oper_module", operModule.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim();
                q.and(w -> w.like("oper_type", kw).or().like("oper_content", kw).or().like("oper_module", kw));
            }
            if (startTime != null && !startTime.trim().isEmpty()) {
                q.ge("create_time", startTime.trim());
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                q.le("create_time", endTime.trim());
            }
            q.orderByDesc("create_time").orderByDesc("id");
            List<AuctionOperLog> list = operLogService.list(q);
            return Result.success("查询成功", new PageInfo<>(list));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private void insertOperLog(HttpSession session, HttpServletRequest request, String module, String type, String content) {
        try {
            Long operUserId = (Long) session.getAttribute("userId");
            if (operUserId == null) return;
            String ip = getClientIp(request);

            AuctionOperLog log = new AuctionOperLog();
            log.setOperUserId(operUserId);
            log.setOperModule(module);
            log.setOperType(type);
            log.setOperContent(content);
            log.setOperIp(ip);
            log.setCreateTime(LocalDateTime.now());
            operLogService.save(log);
        } catch (Exception ignored) {
            // 日志写入失败不影响主流程
        }
    }

    /**
     * 获取客户端IP地址（兼容代理/网关）
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

