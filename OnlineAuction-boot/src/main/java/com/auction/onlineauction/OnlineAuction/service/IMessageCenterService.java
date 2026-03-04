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
     * 获取或创建会话：普通用户针对某商品发起客服咨询
     * 同一用户+同一商品唯一会话；首次创建时随机分配客服
     */
    AuctionMessageSession getOrCreateSession(Long goodsId, Long userId);

    /**
     * 获取或创建管理沟通会话：管理员/超管与内部角色（卖方、客服、拍卖师、财务、运营）建立对话
     * 不允许与普通用户(role=1)建立
     */
    AuctionMessageSession getOrCreateAdminSession(Long adminUserId, Long targetUserId);

    /**
     * 发送消息（文本/订单信息/附件）
     * @param contentType 1=文本 2=订单信息 3=附件
     */
    AuctionMessageCenter sendMessage(Long sessionId, Long senderId, Integer contentType, String content, Long fileId);

    /**
     * 用户端：我的会话列表（普通用户看自己的客服咨询 type=1）
     */
    PageInfo<Map<String, Object>> getMySessions(Long userId, Integer current, Integer size);

    /**
     * 客服端：分配给自己的会话列表（type=1 客服咨询 + type=2 管理沟通中作为接收方）
     */
    PageInfo<Map<String, Object>> getServiceSessions(Long serviceId, Integer current, Integer size);

    /**
     * 管理沟通会话：管理员/超管/卖方/拍卖师/财务/运营 查看 type=2 的会话
     */
    PageInfo<Map<String, Object>> getAdminSessions(Long currentUserId, Integer current, Integer size, boolean isSuperAdmin);

    /**
     * 超级管理员：所有会话列表（兼容旧逻辑，仅 type=1 时用于监管）
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
