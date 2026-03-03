package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageCenter;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageSession;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 消息中心服务（以商品为核心的客服咨询）
 */
public interface IMessageCenterService {

    /**
     * 获取或创建会话：用户针对某商品发起咨询
     * 同一用户+同一商品唯一会话；首次创建时随机分配客服
     */
    AuctionMessageSession getOrCreateSession(Long goodsId, Long userId);

    /**
     * 发送消息（文本/订单信息/附件）
     * @param contentType 1=文本 2=订单信息 3=附件
     */
    AuctionMessageCenter sendMessage(Long sessionId, Long senderId, Integer contentType, String content, Long fileId);

    /**
     * 用户端：我的会话列表（买方/卖方看自己发起的会话）
     */
    PageInfo<Map<String, Object>> getMySessions(Long userId, Integer current, Integer size);

    /**
     * 客服端：分配给自己的会话列表
     */
    PageInfo<Map<String, Object>> getServiceSessions(Long serviceId, Integer current, Integer size);

    /**
     * 超级管理员：所有会话列表
     */
    PageInfo<Map<String, Object>> getAllSessions(Integer current, Integer size, Long goodsId, Long userId);

    /**
     * 获取会话详情（含商品信息）
     */
    Map<String, Object> getSessionDetail(Long sessionId, Long currentUserId);

    /**
     * 获取会话内的消息列表
     */
    List<Map<String, Object>> getSessionMessages(Long sessionId, Long currentUserId);

    /**
     * 标记消息已读
     */
    void markAsRead(Long messageId, Long readerId);

    /**
     * 关闭会话（商品结束后由系统或管理员关闭）
     */
    void closeSession(Long sessionId, Long operatorId);

    /**
     * 检查当前用户是否有权限访问该会话
     */
    boolean canAccessSession(Long sessionId, Long userId, boolean isSuperAdmin);
}
