import request from "@/utils/request";

/**
 * 前台用户登录（买方/卖方）
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
 * 后台管理用户登录
 */
export function adminLogin(userName, password) {
  return request({
    url: "/auth/admin/login",
    method: "post",
    data: {
      userName,
      password,
    },
  });
}

/**
 * 公开注册（仅买方）
 */
export function register(data) {
  return request({
    url: "/auth/register",
    method: "post",
    data,
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
