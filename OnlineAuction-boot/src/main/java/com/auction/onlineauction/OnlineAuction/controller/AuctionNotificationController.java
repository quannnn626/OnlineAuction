package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.service.IAuctionNotificationService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理沟通通知（独立模块 B）
 */
@RestController
@RequestMapping("/api/OnlineAuction/notifications")
public class AuctionNotificationController {

    @Autowired
    private IAuctionNotificationService notificationService;

    @Autowired
    private IAuctionUserService userService;

    /**
     * 后台：选择内部岗位接收人（暂时不加卖家 role=2）
     */
    @GetMapping("/search-staff-users")
    public Result<List<Map<String, Object>>> searchStaffUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !RoleCheckHelper.hasAnyRole(session, 3, 4)) {
                return Result.error("无权限");
            }
            List<Map<String, Object>> list = userService.searchStaffUsersForNotification(keyword, limit != null ? limit : 10);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 后台：发送通知
     * receiverIds：接收人ID列表（单个/批量）
     * needConfirm：是否需要接收人“确认收到”（0/1）
     */
    @PostMapping("/send")
    public Result<Void> send(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !RoleCheckHelper.hasAnyRole(session, 3, 4)) {
                return Result.error("无权限发送");
            }
            Long senderId = (Long) session.getAttribute("userId");
            if (senderId == null) return Result.error("请先登录");

            Object receiverIdsObj = body != null ? body.get("receiverIds") : null;
            List<Long> receiverIds = parseLongList(receiverIdsObj);
            String title = body != null ? (body.get("title") != null ? body.get("title").toString() : null) : null;
            String content = body != null ? (body.get("content") != null ? body.get("content").toString() : null) : null;
            Integer noticeType = body != null && body.get("noticeType") != null ? Integer.valueOf(body.get("noticeType").toString()) : null;

            Integer needConfirm = null;
            if (body != null) {
                Object needConfirmObj = body.get("needConfirm");
                if (needConfirmObj != null) {
                    if (needConfirmObj instanceof Boolean) {
                        needConfirm = ((Boolean) needConfirmObj) ? 1 : 0;
                    } else {
                        needConfirm = Integer.valueOf(needConfirmObj.toString());
                    }
                }
            }

            // 兜底校验：后端强制剔除卖家接收人（role=2），只允许内部岗位（role 5～10）
            receiverIds = filterAllowedReceiverIds(receiverIds);
            if (receiverIds.isEmpty()) {
                return Result.error("接收人无效：不允许发送给卖家/非内部岗位");
            }

            notificationService.sendAdminNotification(senderId, receiverIds, title, content, noticeType, needConfirm);
            return Result.success("发送成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 后台：通知列表（我发起的；超管可看全量）
     */
    @GetMapping("/sent")
    public Result<PageInfo<Map<String, Object>>> getSent(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !RoleCheckHelper.hasAnyRole(session, 3, 4)) {
                return Result.error("无权限");
            }
            Long senderId = (Long) session.getAttribute("userId");
            if (senderId == null) return Result.error("请先登录");

            String roleStr = RoleCheckHelper.getUserRole(session);
            boolean isSuperAdmin = roleStr != null && roleStr.contains("4");

            PageInfo<Map<String, Object>> page = notificationService.getSentNotifications(senderId, current, size, isSuperAdmin);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 后台：查看某条已发通知的接收明细（谁已读、谁已确认）
     */
    @GetMapping("/sent/{notificationId}/targets")
    public Result<List<Map<String, Object>>> getSentTargets(@PathVariable Long notificationId, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !RoleCheckHelper.hasAnyRole(session, 3, 4)) {
                return Result.error("无权限");
            }
            Long senderId = (Long) session.getAttribute("userId");
            if (senderId == null) return Result.error("请先登录");

            String roleStr = RoleCheckHelper.getUserRole(session);
            boolean isSuperAdmin = roleStr != null && roleStr.contains("4");

            List<Map<String, Object>> list = notificationService.getSentNotificationTargetDetails(notificationId, senderId, isSuperAdmin);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 内部岗位：收件箱
     * role=2（卖家）暂不作为接收人
     */
    @GetMapping("/inbox")
    public Result<PageInfo<Map<String, Object>>> inbox(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !RoleCheckHelper.hasAnyRole(session, 5, 6, 7, 8, 9, 10)) {
                return Result.error("无权限查看收件箱");
            }
            Long receiverId = (Long) session.getAttribute("userId");
            if (receiverId == null) return Result.error("请先登录");

            PageInfo<Map<String, Object>> page = notificationService.getInboxNotifications(receiverId, current, size);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 内部岗位：确认收到（同时会标记已读）
     */
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || !RoleCheckHelper.hasAnyRole(session, 5, 6, 7, 8, 9, 10)) {
                return Result.error("无权限");
            }
            Long receiverId = (Long) session.getAttribute("userId");
            if (receiverId == null) return Result.error("请先登录");

            Long notificationId = body != null && body.get("notificationId") != null
                    ? Long.valueOf(body.get("notificationId").toString())
                    : null;
            if (notificationId == null) return Result.error("notificationId不能为空");

            notificationService.confirmReceipt(receiverId, notificationId);
            return Result.success("确认成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private List<Long> parseLongList(Object obj) {
        if (obj == null) return Collections.emptyList();
        List<Long> result = new ArrayList<>();
        if (obj instanceof List) {
            for (Object o : (List<?>) obj) {
                if (o == null) continue;
                result.add(Long.valueOf(o.toString()));
            }
            return result;
        }
        // 兼容单个值
        if (obj instanceof Number) {
            result.add(((Number) obj).longValue());
            return result;
        }
        if (obj instanceof String) {
            String s = ((String) obj).trim();
            if (!s.isEmpty()) {
                if (s.contains(",")) {
                    for (String p : s.split(",")) {
                        if (p == null || p.trim().isEmpty()) continue;
                        result.add(Long.valueOf(p.trim()));
                    }
                } else {
                    result.add(Long.valueOf(s));
                }
            }
        }
        return result;
    }

    private List<Long> filterAllowedReceiverIds(List<Long> receiverIds) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>();
        for (Long rid : receiverIds) {
            if (rid == null) continue;
            AuctionUser u = userService.getById(rid);
            if (u == null || (u.getDelFlag() != null && u.getDelFlag() == 1)) continue;
            String roleStr = u.getUserRole();
            if (roleStr == null) continue;
            // 暂时不加卖家：排除 role=2
            if (roleStr.contains("2")) continue;
            boolean isAllowed = roleStr.contains("5") || roleStr.contains("6") || roleStr.contains("7") || roleStr.contains("8")
                    || roleStr.contains("9") || roleStr.contains("10");
            if (!isAllowed) continue;
            result.add(rid);
        }
        return result.stream().distinct().collect(Collectors.toList());
    }
}

