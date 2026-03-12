package com.auction.onlineauction.OnlineAuction.common;

import javax.servlet.http.HttpSession;
/**
 * 角色校验工具
 * 角色：1=买方 2=卖方 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营
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

    /**
     * 可查看轮播图管理列表（拍卖师、客服、财务仅查看，管理员、超级管理员、运营可查看）
     * 即所有能访问后台且有 admin:banner:view 的角色
     */
    public static boolean canViewBanner(HttpSession session) {
        return hasAnyRole(session, 3, 4, 5, 6, 7, 8);
    }

    /**
     * 可管理轮播图（新增、编辑），管理员、超级管理员、运营
     */
    public static boolean canManageBanner(HttpSession session) {
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

    /**
     * 可查看/管理后台保证金（仅财务、超级管理员，管理员无此权限）
     */
    public static boolean canManageDepositAdmin(HttpSession session) {
        return hasAnyRole(session, 4, 7);
    }

    /**
     * 可查看后台订单列表/详情（管理员、超级管理员、拍卖师、财务、运营）
     */
    public static boolean canViewOrderAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4, 5, 7, 8);
    }

    /**
     * 可管理后台订单（状态更新、结算，不含拍卖师）
     */
    public static boolean canManageOrderAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4, 7);
    }

    /**
     * 可处理退款（财务、管理员、超级管理员）
     */
    public static boolean canProcessRefund(HttpSession session) {
        return hasAnyRole(session, 3, 4, 7);
    }

    /** 可落槌确认成交（拍卖师、管理员、超级管理员） */
    public static boolean canConfirmDeal(HttpSession session) {
        return hasAnyRole(session, 3, 4, 5);
    }

    /** 可发货（卖方本人、管理员、超级管理员、运营） */
    public static boolean canShipOrder(HttpSession session, Long sellerId) {
        if (sellerId == null) return hasAnyRole(session, 3, 4, 8);
        Long uid = getUserId(session);
        if (uid != null && uid.equals(sellerId)) return true;
        return hasAnyRole(session, 3, 4, 8);
    }

    private static Long getUserId(HttpSession session) {
        if (session == null) return null;
        Object v = session.getAttribute("userId");
        if (v instanceof Long) return (Long) v;
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }

    /**
     * 前台留言板可查看（买方 role=1、卖方 role=2）
     */
    public static boolean canViewMessageBoard(HttpSession session) {
        return hasAnyRole(session, 1, 2);
    }

    /**
     * 前台留言板可发布（买方 role=1、卖方 role=2）
     */
    public static boolean canAddMessageBoard(HttpSession session) {
        return hasAnyRole(session, 1, 2);
    }

    /**
     * 前台留言板可编辑自己的留言（买方 role=1、卖方 role=2）
     */
    public static boolean canEditMessageBoard(HttpSession session) {
        return hasAnyRole(session, 1, 2);
    }

    /**
     * 前台留言板可删除自己的留言（买方 role=1、卖方 role=2）
     */
    public static boolean canDeleteMessageBoard(HttpSession session) {
        return hasAnyRole(session, 1, 2);
    }

    /**
     * 后台留言管理可查看（管理员、超级管理员、客服）
     */
    public static boolean canViewMessageAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4, 6);
    }

    /**
     * 后台留言管理可回复（管理员、超级管理员、客服）
     */
    public static boolean canReplyMessageAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4, 6);
    }

    /**
     * 后台留言管理可删除（管理员、超级管理员）
     */
    public static boolean canDeleteMessageAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4);
    }

    /**
     * 可查看后台历史竞拍管理（管理员、超级管理员、拍卖师）
     */
    public static boolean canViewAuctionHistoryAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4, 5);
    }

    /** 消息中心：买方、卖方、管理员、超级管理员、客服、拍卖师、财务、运营可查看（权限细分在会话类型） */
    public static boolean canUseMessageCenter(HttpSession session) {
        return hasAnyRole(session, 1, 2, 3, 4, 5, 6, 7, 8);
    }

    /** 消息中心：超级管理员可查看所有（监管） */
    public static boolean canViewAllMessageCenter(HttpSession session) {
        return hasAnyRole(session, 4);
    }

    /** 是否为客服角色（用于分配会话） */
    public static boolean isCustomerService(HttpSession session) {
        return hasAnyRole(session, 6);
    }

    /** 管理员或超级管理员（可与其他内部角色建立管理沟通） */
    public static boolean isAdminOrSuperAdmin(HttpSession session) {
        return hasAnyRole(session, 3, 4);
    }

    /** 是否为可被管理员沟通的内部角色（卖方、客服、拍卖师、财务、运营，不含买方） */
    public static boolean isStaffRole(int roleType) {
        return roleType == 2 || roleType == 5 || roleType == 6 || roleType == 7 || roleType == 8;
    }
}
