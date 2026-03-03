import request from "@/utils/request";

/** 获取或创建会话（针对某商品） */
export function getOrCreateSession(goodsId) {
  return request({
    url: "/message-center/session",
    method: "post",
    data: { goodsId },
  });
}

/** 发送消息（contentType: 1=文本 2=订单信息 3=附件） */
export function sendMessage(data) {
  return request({
    url: "/message-center/send",
    method: "post",
    data,
  });
}

/** 会话列表 */
export function getSessions(params) {
  return request({
    url: "/message-center/sessions",
    method: "get",
    params,
  });
}

/** 会话详情 */
export function getSessionDetail(id) {
  return request({
    url: `/message-center/session/${id}`,
    method: "get",
  });
}

/** 会话消息列表 */
export function getSessionMessages(id) {
  return request({
    url: `/message-center/session/${id}/messages`,
    method: "get",
  });
}

/** 标记已读 */
export function markAsRead(id) {
  return request({
    url: `/message-center/message/${id}/read`,
    method: "put",
  });
}

/** 关闭会话（超管） */
export function closeSession(id) {
  return request({
    url: `/message-center/session/${id}/close`,
    method: "put",
  });
}
