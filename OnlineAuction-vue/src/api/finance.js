import request from "@/utils/request";

/** 财务：佣金/平台扣费记录分页 */
export function getFeeRecordPage(params) {
  return request({
    url: "/OnlineAuction/auctionFeeRecord/page",
    method: "get",
    params,
  });
}
