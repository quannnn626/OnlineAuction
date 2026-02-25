package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMenu;
import com.auction.onlineauction.OnlineAuction.service.IAuctionMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单API控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/menu")
public class MenuApiController {

    @Autowired
    private IAuctionMenuService menuService;

    /**
     * 根据当前登录用户的角色获取菜单树
     * @param roleType 角色类型（可选）：1=普通用户 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营
     * @param all 是否返回所有菜单（true=返回所有，false=根据角色过滤，默认false）
     * @return 菜单树
     */
    @GetMapping("/tree")
    public Result<List<AuctionMenu>> getMenuTree(
            @RequestParam(required = false) Integer roleType,
            @RequestParam(defaultValue = "false") Boolean all,
            HttpServletRequest request) {
        try {
            List<AuctionMenu> menuTree;
            
            // 如果指定返回所有菜单，直接返回
            if (all) {
                menuTree = menuService.getAllMenuTree();
                return Result.success("获取成功", menuTree);
            }
            
            // 如果指定了roleType，使用指定的角色
            if (roleType != null && roleType > 0) {
                menuTree = menuService.getMenuTreeByRoleType(roleType);
                return Result.success("获取成功", menuTree);
            }
            
            // 否则从Session获取当前登录用户的角色
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录，无法获取菜单");
            }
            
            String userRole = (String) session.getAttribute("userRole");
            if (userRole == null || userRole.trim().isEmpty()) {
                return Result.error("用户角色信息不存在，无法获取菜单");
            }
            
            // 解析用户角色（可能是多个角色，用逗号分隔）
            List<Integer> roleTypes = Arrays.stream(userRole.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            
            if (roleTypes.isEmpty()) {
                return Result.error("用户角色信息无效，无法获取菜单");
            }
            
            // 根据用户的所有角色获取菜单（合并去重）
            menuTree = menuService.getMenuTreeByRoleTypes(roleTypes);
            
            return Result.success("获取成功", menuTree);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}

