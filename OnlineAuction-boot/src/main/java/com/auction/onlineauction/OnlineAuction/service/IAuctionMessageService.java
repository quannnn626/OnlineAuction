package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.util.Map;

/**
 * <p>
 * 留言板表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionMessageService extends IService<AuctionMessage> {

    /**
     * 前台留言板分页（展示用户昵称、头像、回复内容）
     */
    PageInfo<Map<String, Object>> getPublicMessagePage(Integer current, Integer size, String keyword);

    /**
     * 后台留言管理分页
     */
    PageInfo<Map<String, Object>> getAdminMessagePage(Integer current, Integer size, String keyword, Integer messageStatus);

    /**
     * 发布留言
     */
    AuctionMessage addMessage(Long userId, String messageContent, Long goodsId);

    /**
     * 编辑自己的留言
     */
    AuctionMessage updateOwnMessage(Long messageId, Long userId, String messageContent);

    /**
     * 删除自己的留言（逻辑删除）
     */
    void deleteOwnMessage(Long messageId, Long userId);

    /**
     * 后台回复留言
     */
    AuctionMessage replyMessage(Long messageId, String replyContent);

    /**
     * 后台删除留言（逻辑删除）
     */
    void deleteMessageAdmin(Long messageId);
}
