import { request } from "./request";

export interface LoginUser {
  id: number;
  userName: string;
  nickName: string;
  avatar?: string;
  userRole: string;
  needSetPassword?: boolean;
  /** 是否可用网页端登录（已设密码且昵称非默认） */
  webLoginAllowed?: boolean;
  isBuyer?: boolean;
  isSeller?: boolean;
}

export interface BidRecordRow {
  id?: number;
  buyerName?: string;
  buyer_name?: string;
  bidPrice?: number;
  bid_price?: number;
  bidTime?: string;
  bid_time?: string;
  isHighest?: number;
  is_highest?: number;
}

export interface GoodsItem {
  id: number;
  goodsName: string;
  goodsDesc: string;
  currentHighestPrice?: number;
  basePrice?: number;
  shelfStatus?: number;
  fileIds?: string;
  files?: Array<{ filePath: string; fileType?: string }>;
}

export interface SpecialItem {
  id: number;
  specialName: string;
  specialDesc: string;
}

interface PageInfo<T> {
  list: T[];
  total: number;
}

export function wechatLogin(data: {
  code: string;
  phone: string;
  nickName?: string;
  avatar?: string;
}) {
  return request<{ user: LoginUser }>({
    url: "/mp/auth/wechat-login",
    method: "POST",
    data,
  });
}

export function getHomeGoods(current = 1, size = 10, keyword = "") {
  return request<PageInfo<GoodsItem>>({
    url: `/mp/home/goods?current=${current}&size=${size}&keyword=${encodeURIComponent(keyword)}`,
  });
}

export function getSpecialList() {
  return request<SpecialItem[]>({
    url: "/mp/special/list",
  });
}

export function getSpecialGoods(specialId: number) {
  return request<Array<Record<string, unknown>>>({
    url: `/mp/special/${specialId}/goods`,
  });
}

export function getGoodsDetail(goodsId: number) {
  return request<GoodsItem & Record<string, unknown>>({
    url: `/mp/goods/${goodsId}`,
  });
}

/** 与网页端同一接口，依赖登录 Session（Cookie） */
export function submitBid(goodsId: number, bidPrice: number) {
  return request<unknown>({
    url: "/OnlineAuction/auctionRecord/bid",
    method: "POST",
    data: { goodsId, bidPrice },
  });
}

/** 商品竞拍记录（公开，无需登录；小程序仍带 Cookie 无妨） */
export function getBidRecordsByGoodsId(goodsId: number, limit = 20) {
  return request<BidRecordRow[]>({
    url: `/OnlineAuction/auctionRecord/list?goodsId=${goodsId}&limit=${limit}`,
  });
}

export function getMyGoods(current = 1, size = 10) {
  return request<PageInfo<GoodsItem>>({
    url: `/mp/goods/my?current=${current}&size=${size}`,
  });
}

export function getMyProfile() {
  return request<LoginUser & Record<string, unknown>>({
    url: "/mp/user/profile",
  });
}

export function setPassword(newPassword: string) {
  return request<{ needSetPassword: boolean }>({
    url: "/mp/user/set-password",
    method: "POST",
    data: { password: newPassword },
  });
}

/** 同时设置昵称与密码（完成后可使用网页端登录） */
export function completeMpProfile(nickName: string, password: string) {
  return request<{ needSetPassword: boolean; webLoginAllowed: boolean }>({
    url: "/mp/user/complete-profile",
    method: "POST",
    data: { nickName, password },
  });
}
