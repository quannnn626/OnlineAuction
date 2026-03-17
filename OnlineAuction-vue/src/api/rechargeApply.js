import request from "@/utils/request";

/** 用户：我的充值申请列表 */
export function getMyRechargeApplyPage(params) {
  return request({
    url: "/OnlineAuction/auctionDepositRechargeApply/my/page",
    method: "get",
    params,
  });
}

/** 用户：申请充值 */
export function applyRecharge(data) {
  return request({
    url: "/OnlineAuction/auctionDepositRechargeApply/apply",
    method: "post",
    data,
  });
}

/** 财务：充值申请列表 */
export function getAdminRechargeApplyPage(params) {
  return request({
    url: "/OnlineAuction/auctionDepositRechargeApply/admin/page",
    method: "get",
    params,
  });
}

/** 财务：通过充值申请 */
export function approveRechargeApply(id, data) {
  return request({
    url: `/OnlineAuction/auctionDepositRechargeApply/admin/${id}/approve`,
    method: "post",
    data: data || {},
  });
}

/** 财务：驳回充值申请 */
export function rejectRechargeApply(id, data) {
  return request({
    url: `/OnlineAuction/auctionDepositRechargeApply/admin/${id}/reject`,
    method: "post",
    data: data || {},
  });
}
