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

export function applyUserStatus(data) {
  return request({
    url: "/risk/user-status/apply",
    method: "post",
    data,
  });
}

export function getOperLogsPage(params) {
  return request({
    url: "/risk/oper-logs/page",
    method: "get",
    params,
  });
}

