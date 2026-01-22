import request from "@/utils/request";

/**
 * 用户登录
 */
export function login(userName, password) {
  return request({
    url: "/auth/login",
    method: "post",
    data: {
      userName,
      password,
    },
  });
}

/**
 * 获取当前登录用户信息
 */
export function getCurrentUser() {
  return request({
    url: "/auth/current",
    method: "get",
  });
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: "/auth/logout",
    method: "post",
  });
}
