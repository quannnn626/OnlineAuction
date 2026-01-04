package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 系统菜单表 Mapper 接口
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface AuctionMenuMapper extends BaseMapper<AuctionMenu> {

    /**
     * 根据角色类型查询菜单列表
     * @param roleType 角色类型：1=买方用户 2=卖方用户 3=后台管理员
     * @return 菜单列表
     */
    List<AuctionMenu> selectMenusByRoleType(@Param("roleType") Integer roleType);

    /**
     * 查询所有菜单列表（不区分角色权限）
     * @return 菜单列表
     */
    List<AuctionMenu> selectAllMenus();
}
