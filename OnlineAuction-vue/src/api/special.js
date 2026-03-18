import request from "@/utils/request";

export function getSpecialList() {
  return request({ url: "/OnlineAuction/auctionSpecial/list", method: "get" });
}

export function getSpecialById(id) {
  return request({ url: `/OnlineAuction/auctionSpecial/${id}`, method: "get" });
}

export function getSpecialGoods(specialId) {
  return request({ url: `/OnlineAuction/auctionSpecial/${specialId}/goods`, method: "get" });
}

export function addSpecial(data) {
  return request({ url: "/OnlineAuction/auctionSpecial", method: "post", data });
}

export function updateSpecial(id, data) {
  return request({ url: `/OnlineAuction/auctionSpecial/${id}`, method: "put", data });
}

export function deleteSpecial(id) {
  return request({ url: `/OnlineAuction/auctionSpecial/${id}`, method: "delete" });
}

export function addSpecialGoods(specialId, goodsId, sortOrder = 0) {
  return request({
    url: `/OnlineAuction/auctionSpecial/${specialId}/goods`,
    method: "post",
    data: { goodsId, sortOrder },
  });
}

export function removeSpecialGoods(specialId, goodsId) {
  return request({
    url: `/OnlineAuction/auctionSpecial/${specialId}/goods/${goodsId}`,
    method: "delete",
  });
}
