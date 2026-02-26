import request from "@/utils/request";

export function getMyOrderPage(params) {
  return request({ url: "/order/page", method: "get", params });
}

export function getOrderById(id) {
  return request({ url: `/order/${id}`, method: "get" });
}

export function getAdminOrderPage(params) {
  return request({ url: "/OnlineAuction/auctionOrder/page", method: "get", params });
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
