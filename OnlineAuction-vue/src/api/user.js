import request from "@/utils/request";

/**
 * 获取用户列表（分页）
 */
export function getUserPage(params) {
  return request({
    url: "/OnlineAuction/auctionUser/page",
    method: "get",
    params,
  });
}

/**
 * 获取用户详情
 */
export function getUserById(id) {
  return request({
    url: `/OnlineAuction/auctionUser/${id}`,
    method: "get",
  });
}

/**
 * 创建用户
 */
export function createUser(data) {
  return request({
    url: "/OnlineAuction/auctionUser",
    method: "post",
    data,
  });
}

/**
 * 更新用户信息
 */
export function updateUser(id, data) {
  return request({
    url: `/OnlineAuction/auctionUser/${id}`,
    method: "put",
    data,
  });
}

/**
 * 更新用户状态（禁用/恢复）
 */
export function updateUserStatus(id, userStatus) {
  return request({
    url: `/OnlineAuction/auctionUser/${id}/status`,
    method: "put",
    params: {
      userStatus: userStatus,
    },
  });
}

/**
 * 卖方用户资质审核
 */
export function auditSeller(id, auditStatus, auditRemark) {
  return request({
    url: `/OnlineAuction/auctionUser/${id}/seller-audit`,
    method: "put",
    params: {
      auditStatus: auditStatus,
      auditRemark: auditRemark,
    },
  });
}

/**
 * 删除用户（逻辑删除）
 */
export function deleteUser(id) {
  return request({
    url: `/OnlineAuction/auctionUser/${id}`,
    method: "delete",
  });
}

/**
 * 更新个人资料
 */
export function updateProfile(data) {
  return request({
    url: "/OnlineAuction/auctionUser/profile",
    method: "put",
    data,
  });
}

/**
 * 修改密码
 */
export function changePassword(oldPassword, newPassword) {
  return request({
    url: "/OnlineAuction/auctionUser/change-password",
    method: "post",
    data: {
      oldPassword,
      newPassword,
    },
  });
}

/**
 * 更新头像
 */
export function updateAvatar(avatarFileId) {
  return request({
    url: "/OnlineAuction/auctionUser/avatar",
    method: "post",
    params: {
      avatarFileId: avatarFileId,
    },
  });
}
