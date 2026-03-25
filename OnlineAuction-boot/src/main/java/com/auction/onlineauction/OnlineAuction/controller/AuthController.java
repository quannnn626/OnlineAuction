package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.dto.LoginDTO;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOperLogService;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOperLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * <p>
 * 认证控制器（登录、登出等）
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IAuctionUserService userService;
    
    @Autowired
    private IAuctionFileService fileService;

    @Autowired
    private IAuctionOperLogService operLogService;

    /**
     * 前台用户登录（仅买方/卖方账号）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        try {
            String userName = loginRequest.get("userName");
            String password = loginRequest.get("password");
            String loginIp = getClientIp(request);

            LoginDTO loginDTO = userService.loginForPublicPortal(userName, password, loginIp);
            bindUserSession(request, loginDTO);

            insertAuthLog(loginDTO.getId(), "login", "前台登录成功：" + (loginDTO.getUserName() != null ? loginDTO.getUserName() : userName), loginIp);

            Map<String, Object> result = new HashMap<>();
            result.put("user", loginDTO);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            String loginIp = getClientIp(request);
            String userName = loginRequest != null ? loginRequest.get("userName") : null;
            insertAuthLog(null, "login_fail", "前台登录失败：" + (userName != null ? userName : "-") + "，原因=" + e.getMessage(), loginIp);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 后台管理用户登录（须具备岗位角色 3～10）
     */
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        try {
            String userName = loginRequest.get("userName");
            String password = loginRequest.get("password");
            String loginIp = getClientIp(request);

            LoginDTO loginDTO = userService.loginForAdminPortal(userName, password, loginIp);
            bindUserSession(request, loginDTO);

            insertAuthLog(loginDTO.getId(), "login", "后台登录成功：" + (loginDTO.getUserName() != null ? loginDTO.getUserName() : userName), loginIp);

            Map<String, Object> result = new HashMap<>();
            result.put("user", loginDTO);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            String loginIp = getClientIp(request);
            String userName = loginRequest != null ? loginRequest.get("userName") : null;
            insertAuthLog(null, "login_fail", "后台登录失败：" + (userName != null ? userName : "-") + "，原因=" + e.getMessage(), loginIp);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 公开注册（仅创建买方账号）
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String userName = body != null ? body.get("userName") : null;
            String password = body != null ? body.get("password") : null;
            String phone = body != null ? body.get("phone") : null;
            String nickName = body != null ? body.get("nickName") : null;
            userService.registerPublicUser(userName, password, phone, nickName);
            String ip = getClientIp(request);
            insertAuthLog(null, "register", "用户注册：" + userName, ip);
            return Result.success("注册成功，请登录", null);
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    private void bindUserSession(HttpServletRequest request, LoginDTO loginDTO) {
        HttpSession session = request.getSession();
        session.setAttribute("userId", loginDTO.getId());
        session.setAttribute("userName", loginDTO.getUserName());
        session.setAttribute("userRole", loginDTO.getUserRole());
        session.setAttribute("roles", loginDTO.getRoles());
        session.setAttribute("isAdmin", loginDTO.getIsAdmin());
        session.setAttribute("isSuperAdmin", loginDTO.getIsSuperAdmin());
        session.setAttribute("isBuyer", loginDTO.getIsBuyer());
        session.setAttribute("isSeller", loginDTO.getIsSeller());
    }

    /**
     * 获取当前登录用户信息（用于校验 session 是否有效，服务重启后 session 失效时返回 401）
     */
    @GetMapping("/current")
    public ResponseEntity<Result<Map<String, Object>>> getCurrentUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未登录"));
            }
            
            // 从数据库查询完整的用户信息
            com.auction.onlineauction.OnlineAuction.entity.AuctionUser user = userService.getById(userId);
            if (user == null || user.getDelFlag() == 1) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "用户不存在"));
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            // Session中的基本信息
            userInfo.put("userId", userId);
            userInfo.put("userName", user.getUserName());
            userInfo.put("userRole", session.getAttribute("userRole"));
            userInfo.put("roles", session.getAttribute("roles"));
            userInfo.put("isAdmin", session.getAttribute("isAdmin"));
            userInfo.put("isSuperAdmin", session.getAttribute("isSuperAdmin"));
            userInfo.put("isBuyer", session.getAttribute("isBuyer"));
            userInfo.put("isSeller", session.getAttribute("isSeller"));
            
            // 用户详细信息
            userInfo.put("nickName", user.getNickName());
            userInfo.put("realName", user.getRealName());
            userInfo.put("phone", user.getPhone());
            userInfo.put("email", user.getEmail());
            userInfo.put("sex", user.getSex() != null ? user.getSex() : "2");
            // 卖方资质信息（买家申请成为卖家用）
            userInfo.put("sellerAuditStatus", user.getSellerAuditStatus());
            userInfo.put("sellerAuditRemark", user.getSellerAuditRemark());
            userInfo.put("sellerCertificateFiles", user.getSellerCertificateFiles());
            
            // 头像信息
            if (user.getAvatarFileId() != null) {
                // 如果有头像文件ID，查询文件信息获取路径
                com.auction.onlineauction.OnlineAuction.entity.AuctionFile avatarFile = fileService.getById(user.getAvatarFileId());
                if (avatarFile != null && avatarFile.getDelFlag() == 0) {
                    userInfo.put("avatar", avatarFile.getFilePath());
                    userInfo.put("avatarFileId", user.getAvatarFileId());
                } else {
                    // 文件不存在，使用旧的avatar字段
                    userInfo.put("avatar", user.getAvatar());
                    userInfo.put("avatarFileId", null);
                }
            } else if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                // 没有头像文件ID，但有旧的avatar字段
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("avatarFileId", null);
            } else {
                // 都没有，设置为空
                userInfo.put("avatar", null);
                userInfo.put("avatarFileId", null);
            }
            
            return ResponseEntity.ok(Result.success("获取成功", userInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(
                    (Result<Map<String, Object>>) (Result<?>) Result.error("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return Result.success("登出成功", null);
        } catch (Exception e) {
            return Result.error("登出失败：" + e.getMessage());
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
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    private void insertAuthLog(Long operUserId, String operType, String content, String ip) {
        try {
            AuctionOperLog log = new AuctionOperLog();
            log.setOperUserId(operUserId);
            log.setOperModule("auth");
            log.setOperType(operType);
            log.setOperContent(content);
            log.setOperIp(ip);
            log.setCreateTime(LocalDateTime.now());
            operLogService.save(log);
        } catch (Exception ignored) {
            // 日志写入失败不影响主流程
        }
    }
}
