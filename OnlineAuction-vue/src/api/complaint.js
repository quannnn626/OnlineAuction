import request from "@/utils/request";

export function submitComplaint(data) {
  return request({
    url: "/complaint",
    method: "post",
    data,
  });
}
