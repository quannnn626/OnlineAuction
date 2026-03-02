import request from "@/utils/request";

/**
 * 前台：留言板分页
 */
export function getMessagePage(params) {
  return request({
    url: "/message/page",
    method: "get",
    params,
  });
}

/**
 * 前台：发布留言
 */
export function addMessage(data) {
  return request({
    url: "/message",
    method: "post",
    data,
  });
}

/**
 * 前台：编辑留言（仅自己）
 */
export function updateMessage(id, data) {
  return request({
    url: `/message/${id}`,
    method: "put",
    data,
  });
}

/**
 * 前台：删除留言（仅自己）
 */
export function deleteMessage(id) {
  return request({
    url: `/message/${id}`,
    method: "delete",
  });
}

/**
 * 后台：留言管理分页
 */
export function getAdminMessagePage(params) {
  return request({
    url: "/OnlineAuction/auctionMessage/page",
    method: "get",
    params,
  });
}

/**
 * 后台：回复留言
 */
export function replyMessage(id, data) {
  return request({
    url: `/OnlineAuction/auctionMessage/${id}/reply`,
    method: "put",
    data,
  });
}

/**
 * 后台：删除留言
 */
export function deleteAdminMessage(id) {
  return request({
    url: `/OnlineAuction/auctionMessage/${id}`,
    method: "delete",
  });
}
