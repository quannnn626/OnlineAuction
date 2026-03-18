import request from "@/utils/request";

export function getRecommendList(recommendType, targetId) {
  return request({
    url: "/OnlineAuction/auctionRecommend/list",
    method: "get",
    params: { recommendType, targetId },
  });
}

export function addRecommend(data) {
  return request({ url: "/OnlineAuction/auctionRecommend", method: "post", data });
}

export function updateRecommendSort(id, sortOrder) {
  return request({
    url: `/OnlineAuction/auctionRecommend/${id}/sort`,
    method: "put",
    data: { sortOrder },
  });
}

export function removeRecommend(id) {
  return request({ url: `/OnlineAuction/auctionRecommend/${id}`, method: "delete" });
}
