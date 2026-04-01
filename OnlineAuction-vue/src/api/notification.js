import request from "@/utils/request";

/**
 * 后台：搜索内部岗位接收人（暂时不包含卖家 role=2）
 */
export function searchStaffUsers(keyword, limit = 10) {
  return request({
    url: "/OnlineAuction/notifications/search-staff-users",
    method: "get",
    params: {
      keyword,
      limit,
    },
  });
}

/**
 * 后台：发起通知
 * receiverIds：接收人ID数组（单个/批量均可）
 * needConfirm：是否需要“确认收到”（0/1）
 */
export function sendNotification({
  receiverIds,
  title,
  content,
  noticeType,
  needConfirm,
}) {
  return request({
    url: "/OnlineAuction/notifications/send",
    method: "post",
    data: {
      receiverIds,
      title,
      content,
      noticeType,
      needConfirm,
    },
  });
}

/**
 * 后台：我发起过的通知（分页）
 */
export function getSentNotifications(current = 1, size = 10) {
  return request({
    url: "/OnlineAuction/notifications/sent",
    method: "get",
    params: {
      current,
      size,
    },
  });
}

/**
 * 后台：某条已发通知接收详情（谁已读/谁已确认）
 */
export function getSentNotificationTargets(notificationId) {
  return request({
    url: `/OnlineAuction/notifications/sent/${notificationId}/targets`,
    method: "get",
  });
}

/**
 * 内部岗位：通知收件箱（分页）
 */
export function getInboxNotifications(current = 1, size = 10) {
  return request({
    url: "/OnlineAuction/notifications/inbox",
    method: "get",
    params: {
      current,
      size,
    },
  });
}

/**
 * 内部岗位：确认收到/标记已读（后端根据需要确认的 needConfirm 字段决定是否设置 isConfirmed）
 */
export function confirmNotification(notificationId) {
  return request({
    url: "/OnlineAuction/notifications/confirm",
    method: "post",
    data: {
      notificationId,
    },
  });
}

