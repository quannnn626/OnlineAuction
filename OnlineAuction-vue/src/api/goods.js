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
    url: "/OnlineAuction/auctionGoods/search",
    method: "get",
    params,
  });
}

export function deleteGoods(id) {
  return request({
    url: `/OnlineAuction/auctionGoods/${id}`,
    method: "delete",
  });
}
export function getGoodsPage(params) {
  return request({
    url: "/OnlineAuction/auctionGoods/page",
    method: "get",
    params,
  });
}

export function addGoods(data) {
  return request({
    url: "/OnlineAuction/auctionGoods/addGoods",
    method: "post",
    data,
  });
}

export function updateGoods(data) {
  return request({
    url: `/OnlineAuction/auctionGoods/${data.id}`,
    method: "put",
    data,
  });
}

export function auditGoods(data) {
  return request({
    url: `/OnlineAuction/auctionGoods/audit/${data.goodsId}`,
    method: "put",
    params: {
      auditStatus: data.status, // 审核状态作为请求参数
      auditRemark: data.remark // 审核备注作为请求参数
    }
  });
}
export function getMyGoodsList(params) {
  return request({
    url: "/OnlineAuction/auctionGoods/my",
    method: "get",
    params,
  });
}

/**
 * 获取商品详情
 */
export function getGoodsDetail(id) {
  return request({
    url: `/goods/${id}`,
    method: "get",
  });
}