import request from "@/utils/request";

// 根据文件ID列表获取文件列表（后端已支持逗号分隔的 fileIds）
export function getFilesByIds(fileIds) {
  return request({
    url: "/OnlineAuction/auctionFile/list",
    method: "get",
    params: {
      fileIds,
    },
  });
}

