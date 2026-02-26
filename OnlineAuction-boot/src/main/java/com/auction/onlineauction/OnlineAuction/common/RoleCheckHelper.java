package com.auction.onlineauction.OnlineAuction.common;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * 角色校验工具
 * 角色：1=普通用户 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营
 */
public final class RoleCheckHelper {

    /**
     * 从 Session 获取用户角色字符串
     */
    public static String getUserRole(HttpSession session) {
        if (session == null) return null;
        Object v = session.getAttribute("userRole");
        return v != null ? v.toString() : null;
    }

    /**
     * 当前用户是否拥有任一指定角色
     */
    public static boolean hasAnyRole(HttpSession session, int... roleTypes) {
        String roleStr = getUserRole(session);
        if (roleStr == null || roleStr.trim().isEmpty()) return false;
        for (int r : roleTypes) {
            if (roleStr.contains(String.valueOf(r))) return true;
        }
        return false;
    }

    /** 仅管理员(3)或超级管理员(4) 可执行删除类操作 */
    public static boolean canDelete(HttpSession session) {
        return hasAnyRole(session, 3, 4);
    }

    /** 管理员/超级管理员/拍卖师/运营 可审核商品 */
    public static boolean canAuditGoods(HttpSession session) {
        return hasAnyRole(session, 3, 4, 5, 8);
    }

    /** 管理员/超级管理员/运营 可修改商品上架状态 */
    public static boolean canUpdateShelf(HttpSession session) {
        return hasAnyRole(session, 3, 4, 8);
    }

    /** 管理员/超级管理员 可管理类目删除 */
    public static boolean canDeleteCategory(HttpSession session) {
        return hasAnyRole(session, 3, 4);
    }

    /** 管理员/超级管理员 可管理轮播图删除 */
    public static boolean canDeleteBanner(HttpSession session) {
        return hasAnyRole(session, 3, 4);
    }

    /** 管理员/超级管理员/运营 可管理类目、轮播图（增改） */
    public static boolean canManageCategoryOrBanner(HttpSession session) {
        return hasAnyRole(session, 3, 4, 8);
    }

    /** 后台管理员/运营/拍卖师/客服/财务 可访问后台数据 */
    public static boolean canAccessAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4, 5, 6, 7, 8);
    }

    /**
     * 可查看前台竞拍公告（买方、卖方、超级管理员）
     * 拍卖师、客服、财务无 notice:view 权限，不能查看
     */
    public static boolean canViewPublicNotice(HttpSession session) {
        return hasAnyRole(session, 1, 2, 4);
    }

    /**
     * 可管理后台公告（增改查，管理员、超级管理员、运营）
     */
    public static boolean canManageNotice(HttpSession session) {
        return hasAnyRole(session, 3, 4, 8);
    }

    /**
     * 可删除公告（仅管理员、超级管理员）
     */
    public static boolean canDeleteNotice(HttpSession session) {
        return hasAnyRole(session, 3, 4);
    }
}
