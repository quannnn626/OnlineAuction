package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统菜单表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionMenuService extends IService<AuctionMenu> {

    /**
     * 根据角色类型获取菜单树
     * @param roleType 角色类型：1=买方用户 2=卖方用户 3=后台管理员
     * @return 菜单树列表
     */
    List<AuctionMenu> getMenuTreeByRoleType(Integer roleType);

    /**
     * 获取所有菜单树（不区分角色权限）
     * @return 菜单树列表
     */
    List<AuctionMenu> getAllMenuTree();
}
