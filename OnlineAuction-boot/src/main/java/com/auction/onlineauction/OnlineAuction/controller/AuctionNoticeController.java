package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionNotice;
import com.auction.onlineauction.OnlineAuction.service.IAuctionNoticeService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 竞拍公告管理（后台）
 * 权限：管理员(3)、超级管理员(4)、运营(8) 可查看/新增/编辑；仅管理员、超级管理员可删除
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionNotice")
public class AuctionNoticeController {

    @Autowired
    private IAuctionNoticeService noticeService;

    /**
     * 分页查询公告（后台，含未发布、已下架）
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionNotice>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer noticeStatus,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageNotice(request.getSession(false))) {
                return Result.error("无权限查看公告");
            }
            PageInfo<AuctionNotice> page = noticeService.getNoticePage(current, size, keyword, noticeStatus);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取公告详情（后台）
     */
    @GetMapping("/{id}")
    public Result<AuctionNotice> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageNotice(request.getSession(false))) {
                return Result.error("无权限查看公告");
            }
            AuctionNotice n = noticeService.getById(id);
            if (n == null || n.getDelFlag() == 1) {
                return Result.error("公告不存在");
            }
            return Result.success("查询成功", n);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增公告（需 admin:notice:add 权限）
     */
    @PostMapping
    public Result<AuctionNotice> add(@RequestBody AuctionNotice notice, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageNotice(request.getSession(false))) {
                return Result.error("无权限新增公告");
            }
            if (notice.getNoticeTitle() == null || notice.getNoticeTitle().trim().isEmpty()) {
                return Result.error("公告标题不能为空");
            }
            if (notice.getNoticeStatus() == null) notice.setNoticeStatus(0);
            if (notice.getIsTop() == null) notice.setIsTop(0);
            notice.setDelFlag(0);
            if (notice.getNoticeStatus() == 1) {
                notice.setPublishTime(LocalDateTime.now());
            }
            noticeService.save(notice);
            return Result.success("新增成功", notice);
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新公告（需 admin:notice:edit 权限）
     */
    @PutMapping("/{id}")
    public Result<AuctionNotice> update(@PathVariable Long id, @RequestBody AuctionNotice notice,
                                        HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageNotice(request.getSession(false))) {
                return Result.error("无权限编辑公告");
            }
            AuctionNotice exist = noticeService.getById(id);
            if (exist == null || exist.getDelFlag() == 1) {
                return Result.error("公告不存在");
            }
            notice.setId(id);
            if (notice.getNoticeStatus() != null && notice.getNoticeStatus() == 1 && exist.getNoticeStatus() != 1) {
                notice.setPublishTime(LocalDateTime.now());
            }
            noticeService.updateById(notice);
            return Result.success("更新成功", noticeService.getById(id));
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除公告（逻辑删除，需 admin:notice:delete 权限，仅管理员、超级管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canDeleteNotice(request.getSession(false))) {
                return Result.error("无权限删除公告");
            }
            AuctionNotice n = noticeService.getById(id);
            if (n == null || n.getDelFlag() == 1) {
                return Result.error("公告不存在");
            }
            n.setDelFlag(1);
            noticeService.updateById(n);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
