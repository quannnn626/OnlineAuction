package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMenu;
import com.auction.onlineauction.OnlineAuction.service.IAuctionMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 根据角色类型获取菜单树
     * @param roleType 角色类型：1=买方用户 2=卖方用户 3=后台管理员，如果为null或0则返回所有菜单
     * @param all 是否返回所有菜单（true=返回所有，false=根据角色过滤）
     * @return 菜单树
     */
    @GetMapping("/tree")
    public Result<List<AuctionMenu>> getMenuTree(
            @RequestParam(required = false) Integer roleType,
            @RequestParam(defaultValue = "false") Boolean all) {
        try {
            List<AuctionMenu> menuTree;
            if (all || roleType == null || roleType == 0) {
                // 返回所有菜单
                menuTree = menuService.getAllMenuTree();
            } else {
                // 根据角色返回菜单
                menuTree = menuService.getMenuTreeByRoleType(roleType);
            }
            return Result.success("获取成功", menuTree);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}

