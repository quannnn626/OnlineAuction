import request from "@/utils/request";

/**
 * 获取商品分类列表（分页）
 */
export function getCategoryPage(params) {
  return request({
    url: "/OnlineAuction/auctionCategory/page",
    method: "get",
    params,
  });
}

/**
 * 获取所有商品分类列表（不分页）
 */
export function getCategoryList() {
  return request({
    url: "/OnlineAuction/auctionCategory/list",
    method: "get",
  });
}

/**
 * 获取商品分类详情
 */
export function getCategoryById(id) {
  return request({
    url: `/OnlineAuction/auctionCategory/${id}`,
    method: "get",
  });
}

/**
 * 新增商品分类
 */
export function addCategory(data) {
  return request({
    url: "/OnlineAuction/auctionCategory",
    method: "post",
    data,
  });
}

/**
 * 更新商品分类
 */
export function updateCategory(id, data) {
  return request({
    url: `/OnlineAuction/auctionCategory/${id}`,
    method: "put",
    data,
  });
}

/**
 * 删除商品分类
 */
export function deleteCategory(id) {
  return request({
    url: `/OnlineAuction/auctionCategory/${id}`,
    method: "delete",
  });
}

/**
 * 批量删除商品分类
 */
export function batchDeleteCategory(ids) {
  return request({
    url: "/OnlineAuction/auctionCategory/batch",
    method: "delete",
    data: ids,
  });
}

/**
 * 获取商品分类列表（用于首页展示，调用API接口，无需管理员权限）
 */
export function getCategoryListForHome() {
  return request({
    url: "/category/list",
    method: "get",
  });
}

/**
 * 获取商品分类树形结构，用于首页展示
 * @param {Boolean} includeDisabled 是否包含禁用的分类，默认 false
 */
export function getCategoryTreeForHome(includeDisabled = false) {
  return request({
    url: "/category/tree",
    method: "get",
    params: { includeDisabled },
  });
}

/**
 * 获取商品分类树形结构（用于树形控件展示）
 * @param {Boolean} includeDisabled 是否包含禁用的分类，默认false
 */
export function getCategoryTree(includeDisabled = true) {
  return request({
    url: "/OnlineAuction/auctionCategory/tree",
    method: "get",
    params: {
      includeDisabled: includeDisabled,
    },
  });
}

/**
 * 根据父ID获取子分类列表
 */
export function getChildrenByParentId(parentId) {
  return request({
    url: `/OnlineAuction/auctionCategory/children/${parentId}`,
    method: "get",
  });
}
