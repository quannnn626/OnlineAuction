import request from "@/utils/request";

export function getDashboardStats() {
  return request({ url: "/OnlineAuction/dashboard/stats", method: "get" });
}
