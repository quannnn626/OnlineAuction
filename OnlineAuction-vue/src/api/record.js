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
