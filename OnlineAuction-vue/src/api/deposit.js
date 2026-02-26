import request from "@/utils/request";

export function getMyBalance() {
  return request({ url: "/deposit/balance", method: "get" });
}

export function getMyDepositPage(params) {
  return request({ url: "/deposit/page", method: "get", params });
}

export function getAdminDepositPage(params) {
  return request({ url: "/OnlineAuction/auctionDeposit/page", method: "get", params });
}

export function getDepositBalance(userId) {
  return request({ url: `/OnlineAuction/auctionDeposit/balance/${userId}`, method: "get" });
}

export function manualTopUp(data) {
  return request({ url: "/OnlineAuction/auctionDeposit/topUp", method: "post", data });
}
