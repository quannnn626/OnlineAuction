package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionMenu;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMenuMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统菜单表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionMenuServiceImpl extends ServiceImpl<AuctionMenuMapper, AuctionMenu> implements IAuctionMenuService {

    @Override
    public List<AuctionMenu> getMenuTreeByRoleType(Integer roleType) {
        // 查询该角色下的所有菜单
        List<AuctionMenu> menuList = baseMapper.selectMenusByRoleType(roleType);
        
        // 构建菜单树
        return buildMenuTree(menuList, 0L);
    }

    @Override
    public List<AuctionMenu> getMenuTreeByRoleTypes(List<Integer> roleTypes) {
        if (roleTypes == null || roleTypes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 如果包含超级管理员（4），返回所有菜单，但排除留言板菜单（ID=8）
        if (roleTypes.contains(4)) {
            List<AuctionMenu> allMenus = baseMapper.selectAllMenus();
            // 过滤掉留言板菜单（ID=8，路径为 /message-board）
            allMenus = allMenus.stream()
                    .filter(menu -> menu.getId() != 8 && !"/message-board".equals(menu.getMenuPath()))
                    .collect(Collectors.toList());
            return buildMenuTree(allMenus, 0L);
        }
        
        // 合并所有角色的菜单（去重）
        Set<Long> menuIdSet = new HashSet<>();
        List<AuctionMenu> allMenus = new ArrayList<>();
        
        for (Integer roleType : roleTypes) {
            List<AuctionMenu> menus = baseMapper.selectMenusByRoleType(roleType);
            for (AuctionMenu menu : menus) {
                if (!menuIdSet.contains(menu.getId())) {
                    menuIdSet.add(menu.getId());
                    allMenus.add(menu);
                }
            }
        }
        
        // 构建菜单树
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<AuctionMenu> getAllMenuTree() {
        // 查询所有菜单（不区分角色权限）
        List<AuctionMenu> menuList = baseMapper.selectAllMenus();
        
        // 构建菜单树
        return buildMenuTree(menuList, 0L);
    }

    /**
     * 构建菜单树
     * @param menuList 菜单列表
     * @param parentId 父菜单ID
     * @return 菜单树
     */
    private List<AuctionMenu> buildMenuTree(List<AuctionMenu> menuList, Long parentId) {
        List<AuctionMenu> tree = new ArrayList<>();
        for (AuctionMenu menu : menuList) {
            if (menu.getParentId() != null && menu.getParentId().equals(parentId)) {
                // 递归查找子菜单
                List<AuctionMenu> children = buildMenuTree(menuList, menu.getId());
                menu.setChildren(children);
                tree.add(menu);
            }
        }
        return tree;
    }
}
