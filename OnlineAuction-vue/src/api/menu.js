import request from '@/utils/request'

/**
 * 获取菜单树
 * @param {Boolean} all - 是否返回所有菜单
 * @param {Number} roleType - 角色类型
 */
export function getMenuTree(all = false, roleType = null) {
  const params = { all }
  if (roleType) {
    params.roleType = roleType
  }
  return request({
    url: '/menu/tree',
    method: 'get',
    params
  })
}

