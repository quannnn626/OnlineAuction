import request from "@/utils/request";

export function createWorkOrder(data) {
  return request({
    url: "/work-order",
    method: "post",
    data,
  });
}

export function getMyWorkOrderPage(params) {
  return request({
    url: "/work-order/my",
    method: "get",
    params,
  });
}

export function getServiceWorkOrderPage(params) {
  return request({
    url: "/work-order/service",
    method: "get",
    params,
  });
}

export function serviceProcessWorkOrder(id, data) {
  return request({
    url: `/work-order/${id}/service-process`,
    method: "put",
    data,
  });
}

export function getAdminReviewWorkOrderPage(params) {
  return request({
    url: "/work-order/admin-review",
    method: "get",
    params,
  });
}

export function adminReviewWorkOrder(id, data) {
  return request({
    url: `/work-order/${id}/admin-review`,
    method: "put",
    data,
  });
}
