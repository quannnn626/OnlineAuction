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
 * 热门商品分页：每页50条，按点击量倒序
 */
export function getHotGoods(current = 1) {
  return request({
    url: "/goods/hot",
    method: "get",
    params: { current },
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

/**
 * 重新申请上架（卖家操作）
 */
export function reapplyGoods(id) {
  return request({
    url: `/OnlineAuction/auctionGoods/reapply/${id}`,
    method: "put",
  });
}

/**
 * 上架/下架商品（后台）
 */
export function updateShelfStatus(id, shelfStatus) {
  return request({
    url: `/OnlineAuction/auctionGoods/shelf/${id}`,
    method: "put",
    params: { shelfStatus },
  });
}

/**
 * 卖家对自己商品的上下架（我的商品页使用）
 */
export function updateMyGoodsShelf(id, shelfStatus) {
  return request({
    url: `/OnlineAuction/auctionGoods/my/shelf/${id}`,
    method: "put",
    params: { shelfStatus },
  });
}

/** 拍卖师/管理员：拍卖延时（竞拍中商品延长结束时间，单位分钟） */
export function extendAuctionTime(id, minutes = 5) {
  return request({
    url: `/OnlineAuction/auctionGoods/${id}/extend-time`,
    method: "put",
    params: { minutes },
  });
}

/** 拍卖师/管理员：流拍（将竞拍中商品标记为流拍） */
export function markNoSale(id) {
  return request({
    url: `/OnlineAuction/auctionGoods/${id}/mark-no-sale`,
    method: "put",
  });
}

/** 限时拍管理：设置商品竞拍起止时间（管理员、超级管理员、运营） */
export function setGoodsTime(id, data) {
  return request({
    url: `/OnlineAuction/auctionGoods/admin/${id}/time`,
    method: "put",
    data,
  });
}