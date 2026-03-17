import request from "@/utils/request";

/**
 * 提交竞拍出价
 */
export function submitBid(goodsId, bidPrice) {
  return request({
    url: "/OnlineAuction/auctionRecord/bid",
    method: "post",
    data: {
      goodsId,
      bidPrice,
    },
  });
}

/**
 * 查询商品的竞拍记录列表
 */
export function getRecordsByGoodsId(goodsId, limit = 10) {
  return request({
    url: "/OnlineAuction/auctionRecord/list",
    method: "get",
    params: {
      goodsId,
      limit,
    },
  });
}

/**
 * 用户端：历史竞拍商品（按商品聚合）
 */
export function getMyBidGoodsPage(params) {
  return request({
    url: "/OnlineAuction/auctionRecord/my-goods/page",
    method: "get",
    params,
  });
}

/**
 * 用户端：自己在某商品下的竞价记录
 */
export function getMyBidRecordsByGoodsPage(goodsId, params) {
  return request({
    url: `/OnlineAuction/auctionRecord/my-goods/${goodsId}/records/page`,
    method: "get",
    params,
  });
}

/**
 * 后台：历史竞拍商品列表（按商品聚合）
 */
export function getAdminBidGoodsPage(params) {
  return request({
    url: "/OnlineAuction/auctionRecord/admin/goods/page",
    method: "get",
    params,
  });
}

/**
 * 后台：某商品竞拍记录详情
 */
export function getAdminBidRecordsByGoodsPage(goodsId, params) {
  return request({
    url: `/OnlineAuction/auctionRecord/admin/goods/${goodsId}/records/page`,
    method: "get",
    params,
  });
}

/** 拍卖师/管理员：标记异常出价（0=正常 1=恶意出价 2=机器人） */
export function markRecordAbnormal(recordId, type) {
  return request({
    url: `/OnlineAuction/auctionRecord/${recordId}/abnormal`,
    method: "put",
    params: { type },
  });
}
