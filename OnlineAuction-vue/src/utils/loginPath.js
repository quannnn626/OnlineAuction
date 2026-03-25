const STAFF_ROLES = new Set(["3", "4", "5", "6", "7", "8", "9", "10"]);

/**
 * 根据 userRole 判断退出或会话失效后应跳转的登录页
 */
export function loginPathByUserRole(userRole) {
  const roles = userRole
    ? String(userRole)
        .split(",")
        .map((r) => r.trim())
        .filter(Boolean)
    : [];
  return roles.some((r) => STAFF_ROLES.has(r)) ? "/admin/login" : "/login";
}
