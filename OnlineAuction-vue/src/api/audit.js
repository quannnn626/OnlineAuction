import request from "@/utils/request";

export function getAuditOperLogsPage(params) {
  return request({
    url: "/audit/oper-logs/page",
    method: "get",
    params,
  });
}

export function getAuditLoginLogsPage(params) {
  return request({
    url: "/audit/login-logs/page",
    method: "get",
    params,
  });
}

export function getAuditSensitiveLogsPage(params) {
  return request({
    url: "/audit/sensitive-logs/page",
    method: "get",
    params,
  });
}
