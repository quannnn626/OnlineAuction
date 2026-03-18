import request from "@/utils/request";

/** 用户：我的发票申请列表 */
export function getMyInvoicePage(params) {
  return request({
    url: "/OnlineAuction/auctionInvoice/my/page",
    method: "get",
    params,
  });
}

/** 用户：申请发票 */
export function applyInvoice(data) {
  return request({
    url: "/OnlineAuction/auctionInvoice/apply",
    method: "post",
    data,
  });
}

/** 财务：发票申请列表 */
export function getAdminInvoicePage(params) {
  return request({
    url: "/OnlineAuction/auctionInvoice/admin/page",
    method: "get",
    params,
  });
}

/** 用户：获取本人某条发票的发票文件信息（用于下载/查看） */
export function getMyInvoiceFile(invoiceId) {
  return request({
    url: `/OnlineAuction/auctionInvoice/my/${invoiceId}/file`,
    method: "get",
  });
}

/** 财务：处理发票（上传 fileId 或驳回） */
export function handleInvoice(id, data) {
  return request({
    url: `/OnlineAuction/auctionInvoice/admin/${id}/handle`,
    method: "put",
    data,
  });
}
