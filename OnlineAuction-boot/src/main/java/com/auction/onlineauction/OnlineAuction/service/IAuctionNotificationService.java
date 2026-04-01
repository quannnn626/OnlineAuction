package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionNotification;
import com.auction.onlineauction.OnlineAuction.entity.AuctionNotificationTarget;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 管理沟通通知服务
 */
public interface IAuctionNotificationService extends IService<AuctionNotification> {

    /**
     * 管理员发起通知（给一批接收人）
     */
    void sendAdminNotification(
            Long senderId,
            List<Long> receiverIds,
            String title,
            String content,
            Integer noticeType,
            Integer needConfirm
    );

    /**
     * 后台：分页获取“我发起过”的通知列表（包含已读/确认统计）
     */
    PageInfo<Map<String, Object>> getSentNotifications(Long senderId, Integer current, Integer size, boolean isSuperAdmin);

    /**
     * 后台：分页获取“我的通知收件箱”（内部岗位接收人）
     */
    PageInfo<Map<String, Object>> getInboxNotifications(Long receiverId, Integer current, Integer size);

    /**
     * 接收人确认/标记已读
     * - always: is_read=1
     * - if need_confirm=1: is_confirmed=1
     */
    void confirmReceipt(Long receiverId, Long notificationId);

    /**
     * 供 UI 展示：根据通知ID批量获取接收状态（用于已读/确认统计）
     */
    List<AuctionNotificationTarget> listTargetsByNotificationIds(List<Long> notificationIds);

    /**
     * 后台：查看某条通知的接收明细（谁已读、谁已确认）
     */
    List<Map<String, Object>> getSentNotificationTargetDetails(Long notificationId, Long senderId, boolean isSuperAdmin);
}

