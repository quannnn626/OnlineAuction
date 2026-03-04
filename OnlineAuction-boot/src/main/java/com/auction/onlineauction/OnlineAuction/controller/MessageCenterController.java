package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageCenter;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageSession;
import com.auction.onlineauction.OnlineAuction.service.IMessageCenterService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 消息中心 API（单入口，买方/卖方/客服/超级管理员）
 * 以商品为核心的客服咨询会话
 */
@RestController
@RequestMapping("/api/message-center")
public class MessageCenterController {

    @Autowired
    private IMessageCenterService messageCenterService;

    /**
     * 获取或创建会话：普通用户针对某商品发起客服咨询
     */
    @PostMapping("/session")
    public Result<AuctionMessageSession> getOrCreateSession(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canUseMessageCenter(session)) {
                return Result.error("无权限使用消息中心");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            Long goodsId = body.get("goodsId") != null ? Long.valueOf(body.get("goodsId").toString()) : null;
            if (goodsId == null) return Result.error("商品ID不能为空");
            AuctionMessageSession s = messageCenterService.getOrCreateSession(goodsId, userId);
            return Result.success("成功", s);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取或创建管理沟通会话：管理员/超管与内部角色（卖方、客服、拍卖师、财务、运营）建立对话
     */
    @PostMapping("/admin-session")
    public Result<AuctionMessageSession> getOrCreateAdminSession(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.isAdminOrSuperAdmin(session)) {
                return Result.error("无权限建立管理沟通");
            }
            Long adminId = (Long) session.getAttribute("userId");
            if (adminId == null) return Result.error("请先登录");
            Long targetId = body.get("targetUserId") != null ? Long.valueOf(body.get("targetUserId").toString()) : null;
            if (targetId == null) return Result.error("目标用户ID不能为空");
            AuctionMessageSession s = messageCenterService.getOrCreateAdminSession(adminId, targetId);
            return Result.success("成功", s);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送消息（contentType: 1=文本 2=订单信息 3=附件）
     */
    @PostMapping("/send")
    public Result<AuctionMessageCenter> sendMessage(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canUseMessageCenter(session)) {
                return Result.error("无权限发送消息");
            }
            Long senderId = (Long) session.getAttribute("userId");
            if (senderId == null) return Result.error("请先登录");
            Long sessionId = body.get("sessionId") != null ? Long.valueOf(body.get("sessionId").toString()) : null;
            Integer contentType = body.get("contentType") != null ? Integer.valueOf(body.get("contentType").toString()) : 1;
            String content = body.get("content") != null ? body.get("content").toString() : null;
            Long fileId = body.get("fileId") != null ? Long.valueOf(body.get("fileId").toString()) : null;
            if (sessionId == null) return Result.error("会话ID不能为空");
            if (contentType == 3 && fileId == null) return Result.error("附件消息需提供fileId");
            if (contentType != 3 && (content == null || content.trim().isEmpty())) return Result.error("文本内容不能为空");
            AuctionMessageCenter msg = messageCenterService.sendMessage(sessionId, senderId, contentType, content, fileId);
            return Result.success("发送成功", msg);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 会话列表（根据角色返回：用户看自己的，客服看分配的，超管看全部）
     */
    @GetMapping("/sessions")
    public Result<PageInfo<Map<String, Object>>> getSessions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long goodsId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canUseMessageCenter(session) && !RoleCheckHelper.canViewAllMessageCenter(session)) {
                return Result.error("无权限查看消息中心");
            }
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) return Result.error("请先登录");

            PageInfo<Map<String, Object>> page;
            if (RoleCheckHelper.isCustomerService(session)) {
                page = messageCenterService.getServiceSessions(currentUserId, current, size);
            } else if (RoleCheckHelper.isAdminOrSuperAdmin(session) || RoleCheckHelper.hasAnyRole(session, 2, 5, 7, 8)) {
                boolean isSuperAdmin = RoleCheckHelper.canViewAllMessageCenter(session);
                page = messageCenterService.getAdminSessions(currentUserId, current, size, isSuperAdmin);
            } else {
                page = messageCenterService.getMySessions(currentUserId, current, size);
            }
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 会话详情
     */
    @GetMapping("/session/{id}")
    public Result<Map<String, Object>> getSessionDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            boolean isSuperAdmin = RoleCheckHelper.canViewAllMessageCenter(session);
            if (!messageCenterService.canAccessSession(id, userId, isSuperAdmin)) {
                return Result.error("无权限查看该会话");
            }
            Map<String, Object> detail = messageCenterService.getSessionDetail(id, userId);
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 会话消息列表
     */
    @GetMapping("/session/{id}/messages")
    public Result<List<Map<String, Object>>> getSessionMessages(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            boolean isSuperAdmin = RoleCheckHelper.canViewAllMessageCenter(session);
            if (!messageCenterService.canAccessSession(id, userId, isSuperAdmin)) {
                return Result.error("无权限查看该会话");
            }
            List<Map<String, Object>> list = messageCenterService.getSessionMessages(id, userId);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/message/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            messageCenterService.markAsRead(id, userId);
            return Result.success("已读", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 关闭会话（管理员/超管/客服可关闭自己参与的会话）
     */
    @PutMapping("/session/{id}/close")
    public Result<Void> closeSession(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            boolean isSuperAdmin = RoleCheckHelper.canViewAllMessageCenter(session);
            if (!messageCenterService.canAccessSession(id, userId, isSuperAdmin)) {
                return Result.error("无权限关闭该会话");
            }
            messageCenterService.closeSession(id, userId);
            return Result.success("关闭成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
