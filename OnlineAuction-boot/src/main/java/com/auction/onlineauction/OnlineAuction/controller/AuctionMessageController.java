package com.auction.onlineauction.OnlineAuction.controller;


import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessage;
import com.auction.onlineauction.OnlineAuction.service.IAuctionMessageService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 留言板控制器（前台 + 后台）
 */
@RestController
@RequestMapping("/api")
public class AuctionMessageController {

    @Autowired
    private IAuctionMessageService messageService;

    /**
     * 前台：留言板分页
     */
    @GetMapping("/message/page")
    public Result<PageInfo<Map<String, Object>>> getPublicPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canViewMessageBoard(session)) {
                return Result.error("无权限查看留言板");
            }
            PageInfo<Map<String, Object>> page = messageService.getPublicMessagePage(current, size, keyword);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 前台：发布留言
     */
    @PostMapping("/message")
    public Result<AuctionMessage> addMessage(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canAddMessageBoard(session)) {
                return Result.error("无权限发布留言");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            String messageContent = body.get("messageContent") != null ? body.get("messageContent").toString() : null;
            Long goodsId = body.get("goodsId") != null ? Long.valueOf(body.get("goodsId").toString()) : null;
            AuctionMessage message = messageService.addMessage(userId, messageContent, goodsId);
            return Result.success("发布成功", message);
        } catch (Exception e) {
            return Result.error("发布失败：" + e.getMessage());
        }
    }

    /**
     * 前台：编辑自己的留言
     */
    @PutMapping("/message/{id}")
    public Result<AuctionMessage> updateMessage(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canEditMessageBoard(session)) {
                return Result.error("无权限编辑留言");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            String messageContent = body.get("messageContent") != null ? body.get("messageContent").toString() : null;
            AuctionMessage message = messageService.updateOwnMessage(id, userId, messageContent);
            return Result.success("更新成功", message);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 前台：删除自己的留言
     */
    @DeleteMapping("/message/{id}")
    public Result<Void> deleteOwnMessage(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canDeleteMessageBoard(session)) {
                return Result.error("无权限删除留言");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");
            messageService.deleteOwnMessage(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 后台：留言管理分页
     */
    @GetMapping("/OnlineAuction/auctionMessage/page")
    public Result<PageInfo<Map<String, Object>>> getAdminPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer messageStatus,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (!RoleCheckHelper.canViewMessageAdmin(session)) {
                return Result.error("无权限查看留言管理");
            }
            PageInfo<Map<String, Object>> page =
                    messageService.getAdminMessagePage(current, size, keyword, messageStatus);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 后台：回复留言
     */
    @PutMapping("/OnlineAuction/auctionMessage/{id}/reply")
    public Result<AuctionMessage> reply(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (!RoleCheckHelper.canReplyMessageAdmin(session)) {
                return Result.error("无权限回复留言");
            }
            String replyContent = body.get("replyContent") != null ? body.get("replyContent").toString() : null;
            AuctionMessage msg = messageService.replyMessage(id, replyContent);
            return Result.success("回复成功", msg);
        } catch (Exception e) {
            return Result.error("回复失败：" + e.getMessage());
        }
    }

    /**
     * 后台：删除留言
     */
    @DeleteMapping("/OnlineAuction/auctionMessage/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (!RoleCheckHelper.canDeleteMessageAdmin(session)) {
                return Result.error("无权限删除留言");
            }
            messageService.deleteMessageAdmin(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
