import request from "@/utils/request";

export function getAbnormalBidsPage(params) {
  return request({
    url: "/risk/abnormal-bids/page",
    method: "get",
    params,
  });
}

export function getMaliciousBiddersPage(params) {
  return request({
    url: "/risk/malicious-bidders/page",
    method: "get",
    params,
  });
}

export function getRingBiddersPage(params) {
  return request({
    url: "/risk/ring-bidders/page",
    method: "get",
    params,
  });
}

export function getUserRisk(id) {
  return request({
    url: `/risk/users/${id}`,
    method: "get",
  });
}

export function updateUserRiskLevel(id, data) {
  return request({
    url: `/risk/users/${id}/risk-level`,
    method: "post",
    data,
  });
}

/** 有风险行为的用户分页（勾选后批量操作） */
export function getRiskActivityUsersPage(params) {
  return request({
    url: "/risk/users/risk-activity/page",
    method: "get",
    params,
  });
}

export function updateUserRiskLevelBatch(data) {
  return request({
    url: "/risk/users/risk-level/batch",
    method: "post",
    data,
  });
}

export function applyUserStatus(data) {
  return request({
    url: "/risk/user-status/apply",
    method: "post",
    data,
  });
}

/** 批量提交冻结/解封工单（管理员复核） */
export function applyUserStatusBatch(data) {
  return request({
    url: "/risk/user-status/apply-batch",
    method: "post",
    data,
  });
}

