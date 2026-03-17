import request from "@/utils/request";

export function getMyBalance() {
  return request({ url: "/deposit/balance", method: "get" });
}

export function getMyDepositPage(params) {
  return request({ url: "/deposit/page", method: "get", params });
}

/** 平台保证金汇总 */
export function getPlatformSummary() {
  return request({
    url: "/OnlineAuction/auctionDeposit/platformSummary",
    method: "get",
  });
}

/** 用户保证金汇总分页（按用户名/昵称搜索） */
export function getDepositSummaryPage(params) {
  return request({
    url: "/OnlineAuction/auctionDeposit/summaryPage",
    method: "get",
    params,
  });
}

export function getAdminDepositPage(params) {
  return request({
    url: "/OnlineAuction/auctionDeposit/page",
    method: "get",
    params,
  });
}

export function getDepositBalance(userId) {
  return request({
    url: `/OnlineAuction/auctionDeposit/balance/${userId}`,
    method: "get",
  });
}

export function manualTopUp(data) {
  return request({
    url: "/OnlineAuction/auctionDeposit/topUp",
    method: "post",
    data,
  });
}

/** 财务：冻结用户保证金 */
export function freezeDeposit(data) {
  return request({
    url: "/OnlineAuction/auctionDeposit/freeze",
    method: "post",
    data,
  });
}

/** 财务：解冻用户保证金 */
export function unfreezeDeposit(data) {
  return request({
    url: "/OnlineAuction/auctionDeposit/unfreeze",
    method: "post",
    data,
  });
}
