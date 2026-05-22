import request from "@/utils/request";

export function getAddressList() {
  return request({
    url: "/OnlineAuction/auctionAddress/list",
    method: "get",
  });
}

export function saveAddress(data) {
  return request({
    url: "/OnlineAuction/auctionAddress",
    method: "post",
    data,
  });
}

export function deleteAddress(id) {
  return request({
    url: `/OnlineAuction/auctionAddress/${id}`,
    method: "delete",
  });
}

export function setDefaultAddress(id) {
  return request({
    url: `/OnlineAuction/auctionAddress/${id}/default`,
    method: "put",
  });
}
