import request from "@/utils/request";

export function getMyOrderPage(params) {
  return request({ url: "/order/page", method: "get", params });
}

export function getOrderById(id) {
  return request({ url: `/order/${id}`, method: "get" });
}

/** 买方确认收货 */
export function confirmReceipt(id) {
  return request({ url: `/order/${id}/confirm-receipt`, method: "put" });
}

export function getAdminOrderPage(params) {
  return request({ url: "/OnlineAuction/auctionOrder/page", method: "get", params });
}

/** 客服查看某用户的订单（仅在与该用户有会话时可用） */
export function getServiceUserOrderPage(userId, params) {
  return request({
    url: `/OnlineAuction/auctionOrder/service/user/${userId}/page`,
    method: "get",
    params,
  });
}

export function getAdminOrderById(id) {
  return request({ url: `/OnlineAuction/auctionOrder/${id}`, method: "get" });
}

export function updateOrderStatus(id, orderStatus) {
  return request({ url: `/OnlineAuction/auctionOrder/${id}/status`, method: "put", data: { orderStatus } });
}

export function processRefund(id, remark) {
  return request({ url: `/OnlineAuction/auctionOrder/${id}/refund`, method: "post", data: { remark } });
}

/** 落槌确认 */
export function confirmDeal(id) {
  return request({ url: `/OnlineAuction/auctionOrder/${id}/confirm-deal`, method: "post" });
}

/** 发货 */
export function shipOrder(id, expressCompany, expressNo) {
  return request({
    url: `/OnlineAuction/auctionOrder/${id}/ship`,
    method: "put",
    data: { expressCompany, expressNo },
  });
}

/** 拍卖师/管理员：标记悔拍（待付款订单，扣除保证金，商品恢复上架） */
export function markOrderDefault(id) {
  return request({
    url: `/OnlineAuction/auctionOrder/${id}/mark-default`,
    method: "post",
  });
}
