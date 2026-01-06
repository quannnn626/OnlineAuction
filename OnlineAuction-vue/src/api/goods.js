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

export function getMyGoodsList(params) {
  return request({
    url: "/goods/my",
    method: "get",
    params,
  });
}
