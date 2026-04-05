package com.auction.onlineauction.OnlineAuction.controller.mp;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.dto.LoginDTO;
import com.auction.onlineauction.OnlineAuction.dto.mp.MpWechatLoginRequest;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionSpecial;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionSpecialService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序专用接口（独立目录，不复用后台管理接口）
 */
@RestController
@RequestMapping("/api/mp")
public class MpApiController {

    @Autowired
    private IAuctionUserService userService;

    @Autowired
    private IAuctionGoodsService goodsService;

    @Autowired
    private IAuctionSpecialService specialService;

    @Autowired
    private WxMaService wxMaService;

    @PostMapping("/auth/wechat-login")
    public Result<Map<String, Object>> wechatLogin(@RequestBody MpWechatLoginRequest req, HttpServletRequest request) {
        try {
            String code = req != null ? req.getCode() : null;
            if (code == null || code.trim().isEmpty()) {
                return Result.error("code不能为空");
            }
            WxMaJscode2SessionResult sessionResult = wxMaService.getUserService().getSessionInfo(code);
            if (sessionResult == null || sessionResult.getOpenid() == null || sessionResult.getOpenid().trim().isEmpty()) {
                return Result.error("微信登录失败，未获取到openid");
            }
            LoginDTO loginDTO = userService.loginOrRegisterByWechat(
                    sessionResult.getOpenid(),
                    req != null ? req.getPhone() : null,
                    req != null ? req.getNickName() : null,
                    req != null ? req.getAvatar() : null,
                    getClientIp(request)
            );

            HttpSession session = request.getSession();
            session.setAttribute("userId", loginDTO.getId());
            session.setAttribute("userName", loginDTO.getUserName());
            session.setAttribute("userRole", loginDTO.getUserRole());
            session.setAttribute("roles", loginDTO.getRoles());
            session.setAttribute("isAdmin", loginDTO.getIsAdmin());
            session.setAttribute("isSuperAdmin", loginDTO.getIsSuperAdmin());
            session.setAttribute("isBuyer", loginDTO.getIsBuyer());
            session.setAttribute("isSeller", loginDTO.getIsSeller());

            Map<String, Object> result = new HashMap<>();
            result.put("user", loginDTO);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/profile")
    public Result<AuctionUser> getProfile(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        AuctionUser user = userService.getUserByIdWithoutPassword(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success("查询成功", user);
    }

    @GetMapping("/home/goods")
    public Result<PageInfo<AuctionGoods>> getHomeGoods(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String categoryId,
            HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.error("未登录");
        }
        try {
            PageInfo<AuctionGoods> pageInfo = goodsService.getGoodsListForApi(current, size, keyword, status, categoryId);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/special/list")
    public Result<List<AuctionSpecial>> listSpecial(HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.error("未登录");
        }
        QueryWrapper<AuctionSpecial> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        q.eq("status", 1);
        q.orderByAsc("sort_order");
        return Result.success("查询成功", specialService.list(q));
    }

    @GetMapping("/special/{id}/goods")
    public Result<List<Map<String, Object>>> listSpecialGoods(@PathVariable Long id, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.error("未登录");
        }
        AuctionSpecial s = specialService.getById(id);
        if (s == null || s.getDelFlag() == 1 || s.getStatus() == null || s.getStatus() != 1) {
            return Result.error("专场不存在");
        }
        return Result.success("查询成功", specialService.listGoodsBySpecialId(id));
    }

    @GetMapping("/goods/my")
    public Result<PageInfo<AuctionGoods>> getMyGoods(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        try {
            PageInfo<AuctionGoods> pageInfo = goodsService.getMyGoodsList(current, size, userId);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/goods/{id}")
    public Result<AuctionGoods> getGoodsDetail(@PathVariable Long id, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.error("未登录");
        }
        try {
            goodsService.incrementViewCount(id);
            AuctionGoods goods = goodsService.getGoodsByIdForPublic(id);
            return Result.success("查询成功", goods);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/user/set-password")
    public Result<Map<String, Object>> setPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }
            String password = body != null ? body.get("password") : null;
            userService.setPassword(userId, password);

            Map<String, Object> result = new HashMap<>();
            result.put("needSetPassword", false);
            return Result.success("设置成功", result);
        } catch (Exception e) {
            return Result.error("设置失败：" + e.getMessage());
        }
    }

    /**
     * 小程序：同时设置昵称与密码（与仅设密码相比，完成后可使用网页端登录）
     */
    @PostMapping("/user/complete-profile")
    public Result<Map<String, Object>> completeMpProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }
            String nickName = body != null ? body.get("nickName") : null;
            String password = body != null ? body.get("password") : null;
            userService.completeMpProfile(userId, nickName, password);
            Map<String, Object> result = new HashMap<>();
            result.put("needSetPassword", false);
            result.put("webLoginAllowed", true);
            return Result.success("设置成功", result);
        } catch (Exception e) {
            return Result.error("设置失败：" + e.getMessage());
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object uid = session.getAttribute("userId");
        if (uid instanceof Long) return (Long) uid;
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        try {
            return uid == null ? null : Long.valueOf(String.valueOf(uid));
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}
