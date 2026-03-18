import request from "@/utils/request";

/** 财务：佣金/平台扣费记录分页 */
export function getFeeRecordPage(params) {
  return request({
    url: "/OnlineAuction/auctionFeeRecord/page",
    method: "get",
    params,
  });
}

/** 获取佣金比例 */
export function getCommissionRate() {
  return request({ url: "/OnlineAuction/auctionFeeRecord/commissionRate", method: "get" });
}

/** 设置佣金比例（0～1，如 0.05 表示 5%） */
export function setCommissionRate(commissionRate) {
  return request({
    url: "/OnlineAuction/auctionFeeRecord/commissionRate",
    method: "put",
    data: { commissionRate },
  });
}

/** 待佣金结算的订单（已完成且未结算） */
export function getOrdersPendingCommission(params) {
  return request({
    url: "/OnlineAuction/auctionFeeRecord/ordersPendingCommission",
    method: "get",
    params,
  });
}

/** 佣金结算确认 */
export function confirmCommissionSettle(orderId, overrideAmount) {
  return request({
    url: `/OnlineAuction/auctionFeeRecord/confirmSettle/${orderId}`,
    method: "post",
    data: overrideAmount != null ? { overrideAmount } : {},
  });
}

/** 导出扣费记录（对账） */
export function exportFeeRecords(params) {
  return request({
    url: "/OnlineAuction/auctionFeeRecord/export",
    method: "get",
    params,
  });
}
