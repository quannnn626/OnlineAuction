import request from "@/utils/request";

/**
 * 前台：获取已启用的轮播图列表（首页展示，无需登录）
 */
export function getBannerList() {
  return request({
    url: "/banner/list",
    method: "get",
  });
}

/**
 * 后台：获取轮播图列表
 */
export function getAdminBannerList() {
  return request({
    url: "/OnlineAuction/auctionBanner/list",
    method: "get",
  });
}

/**
 * 后台：获取轮播图详情
 */
export function getBannerById(id) {
  return request({
    url: `/OnlineAuction/auctionBanner/${id}`,
    method: "get",
  });
}

/**
 * 后台：新增轮播图
 */
export function addBanner(data) {
  return request({
    url: "/OnlineAuction/auctionBanner",
    method: "post",
    data,
  });
}

/**
 * 后台：更新轮播图
 */
export function updateBanner(id, data) {
  return request({
    url: `/OnlineAuction/auctionBanner/${id}`,
    method: "put",
    data,
  });
}

/**
 * 后台：删除轮播图
 */
export function deleteBanner(id) {
  return request({
    url: `/OnlineAuction/auctionBanner/${id}`,
    method: "delete",
  });
}
