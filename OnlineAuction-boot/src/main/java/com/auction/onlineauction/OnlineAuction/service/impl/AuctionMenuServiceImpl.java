package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionMenu;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMenuMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
