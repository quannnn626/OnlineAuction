import request from "@/utils/request";

export function getGoodsList(params) {
  return request({
    url: "/goods/list",
    method: "get",
    params,
  });
}

export function searchGoods(params) {
  return request({
    url: "/goods/search",
    method: "get",
    params,
  });
}

export function deleteGoods(id) {
  return request({
    url: `/goods/${id}`,
    method: "delete",
  });
}
export function getGoodsPage(params) {
  return request({
    url: "/goods/page",
    method: "get",
    params,
  });
}

export function addGoods(data) {
  return request({
    url: "/goods",
    method: "post",
    data,
  });
}

export function updateGoods(data) {
  return request({
    url: "/goods",
    method: "put",
    data,
  });
}

export function auditGoods(data) {
  return request({
    url: "/goods/audit",
    method: "post",
    data,
  });
}
export function getMyGoodsList(params) {
  return request({
    url: "/goods/my",
    method: "get",
    params,
  });
}
