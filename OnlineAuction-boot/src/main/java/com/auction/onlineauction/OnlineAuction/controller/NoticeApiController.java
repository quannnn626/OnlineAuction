package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionNotice;
import com.auction.onlineauction.OnlineAuction.service.IAuctionNoticeService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 竞拍公告 API（前台，仅已发布公告）
 * 权限：买方(1)、卖方(2)、超级管理员(4) 可查看
 * 拍卖师、客服、财务等无 notice:view 权限，不可查看
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeApiController {

    @Autowired
    private IAuctionNoticeService noticeService;

    /**
     * 分页获取已发布的公告
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionNotice>> getPublishedPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            if (request.getSession(false) == null) {
                return Result.error("请先登录");
            }
            if (!RoleCheckHelper.canViewPublicNotice(request.getSession(false))) {
                return Result.error("无权限查看公告");
            }
            PageInfo<AuctionNotice> page = noticeService.getPublishedNoticePage(current, size, keyword);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取已发布公告详情
     */
    @GetMapping("/{id}")
    public Result<AuctionNotice> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (request.getSession(false) == null) {
                return Result.error("请先登录");
            }
            if (!RoleCheckHelper.canViewPublicNotice(request.getSession(false))) {
                return Result.error("无权限查看公告");
            }
            AuctionNotice n = noticeService.getById(id);
            if (n == null || n.getDelFlag() == 1 || n.getNoticeStatus() != 1) {
                return Result.error("公告不存在或已下架");
            }
            return Result.success("查询成功", n);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
