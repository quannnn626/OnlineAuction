import request from "@/utils/request";

/**
 * 前台：分页获取已发布的公告
 */
export function getPublishedNoticePage(params) {
  return request({
    url: "/notice/page",
    method: "get",
    params,
  });
}

/**
 * 前台：获取已发布公告详情
 */
export function getNoticeDetail(id) {
  return request({
    url: `/notice/${id}`,
    method: "get",
  });
}

/**
 * 后台：分页获取公告列表（含未发布、已下架）
 */
export function getAdminNoticePage(params) {
  return request({
    url: "/OnlineAuction/auctionNotice/page",
    method: "get",
    params,
  });
}

/**
 * 后台：获取公告详情
 */
export function getAdminNoticeById(id) {
  return request({
    url: `/OnlineAuction/auctionNotice/${id}`,
    method: "get",
  });
}

/**
 * 后台：新增公告
 */
export function addNotice(data) {
  return request({
    url: "/OnlineAuction/auctionNotice",
    method: "post",
    data,
  });
}

/**
 * 后台：更新公告
 */
export function updateNotice(id, data) {
  return request({
    url: `/OnlineAuction/auctionNotice/${id}`,
    method: "put",
    data,
  });
}

/**
 * 后台：删除公告
 */
export function deleteNotice(id) {
  return request({
    url: `/OnlineAuction/auctionNotice/${id}`,
    method: "delete",
  });
}
