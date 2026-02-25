package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionUser")
public class AuctionUserController {

    @Autowired
    private IAuctionUserService userService;

    /**
     * 分页查询用户列表
     * 权限说明：
     * - 超级管理员可以查看所有用户，除了超级管理员自己
     * - 管理员可以查看所有普通用户（买方和卖方），无法查看其他管理员以及超级管理员
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionUser>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer userRole,
            @RequestParam(required = false) Integer userStatus,
            HttpServletRequest request) {
        try {
            // 从Session获取当前登录用户ID
            HttpSession session = request.getSession(false);
            Long currentUserId = null;
            if (session != null) {
                currentUserId = (Long) session.getAttribute("userId");
            }
            
            PageInfo<AuctionUser> pageInfo = userService.getUserPage(current, size, userName, userRole, userStatus, currentUserId);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{id}")
    public Result<AuctionUser> getUserById(@PathVariable Long id) {
        try {
            AuctionUser user = userService.getUserByIdWithoutPassword(id);
            return Result.success("查询成功", user);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 创建用户（管理员创建，可选择角色）
     * 权限说明：
     * - 超级管理员可以创建所有角色（买方、卖方、管理员、超级管理员）
     * - 管理员只能创建买方和卖方账号
     */
    @PostMapping
    public Result<AuctionUser> createUser(@RequestBody AuctionUser user, HttpServletRequest request) {
        try {
            // 从Session获取当前登录用户ID
            // 先尝试获取现有Session，如果不存在则创建新Session（用于调试）
            HttpSession session = request.getSession(false);
            if (session == null) {
                session = request.getSession(true);
            }
            
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error(401, "未登录，无法创建用户，请重新登录");
            }
            AuctionUser createdUser = userService.createUser(user, currentUserId);
            return Result.success("创建成功", createdUser);
        } catch (Exception e) {
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<AuctionUser> updateUser(@PathVariable Long id, @RequestBody AuctionUser user, HttpServletRequest request) {
        try {
            // 从Session获取当前登录用户ID
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录，无法更新用户");
            }
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录，无法更新用户");
            }
            
            AuctionUser updatedUser = userService.updateUser(id, user, currentUserId);
            return Result.success("更新成功", updatedUser);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 禁用/恢复用户账号
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer userStatus) {
        try {
            userService.updateUserStatus(id, userStatus);
            String message = userStatus == 0 ? "账号已恢复" : "账号已禁用";
            return Result.success(message, null);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 买方用户申请成为卖方用户
     */
    @PostMapping("/{id}/apply-seller")
    public Result<Void> applySeller(@PathVariable Long id,
                                    @RequestParam String certificateFiles) {
        try {
            userService.applySeller(id, certificateFiles);
            return Result.success("申请已提交，请等待管理员审核", null);
        } catch (Exception e) {
            return Result.error("申请失败：" + e.getMessage());
        }
    }

    /**
     * 卖方用户资质审核（管理员审核）
     */
    @PutMapping("/{id}/seller-audit")
    public Result<Void> auditSeller(@PathVariable Long id, 
                                    @RequestParam Integer auditStatus,
                                    @RequestParam(required = false) String auditRemark,
                                    @RequestParam(required = false) Long auditUserId) {
        try {
            userService.auditSeller(id, auditStatus, auditRemark, auditUserId);
            String message = auditStatus == 2 ? "审核通过，用户已获得卖方身份" : "审核驳回";
            return Result.success(message, null);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新个人资料（用户自己更新）
     */
    @PutMapping("/profile")
    public Result<AuctionUser> updateProfile(@RequestBody AuctionUser user, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error(401, "未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            
            AuctionUser updatedUser = userService.updateProfile(userId, user);
            return Result.success("更新成功", updatedUser);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> requestData, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error(401, "未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            
            String oldPassword = requestData.get("oldPassword");
            String newPassword = requestData.get("newPassword");
            
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error("密码修改失败：" + e.getMessage());
        }
    }

    /**
     * 上传并更新头像
     */
    @PostMapping("/avatar")
    public Result<AuctionUser> updateAvatar(@RequestParam("avatarFileId") Long avatarFileId, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error(401, "未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            
            AuctionUser updatedUser = userService.updateAvatar(userId, avatarFileId);
            return Result.success("头像更新成功", updatedUser);
        } catch (Exception e) {
            return Result.error("头像更新失败：" + e.getMessage());
        }
    }
}
