package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionMessage;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMessageMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionMessageService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 留言板表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionMessageServiceImpl extends ServiceImpl<AuctionMessageMapper, AuctionMessage> implements IAuctionMessageService {

    @Autowired
    private IAuctionUserService userService;

    @Autowired
    private IAuctionFileService fileService;

    @Override
    public PageInfo<Map<String, Object>> getPublicMessagePage(Integer current, Integer size, String keyword) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionMessage> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            q.like("message_content", keyword.trim());
        }
        q.orderByDesc("create_time");
        List<AuctionMessage> list = list(q);
        return toViewPage(list);
    }

    @Override
    public PageInfo<Map<String, Object>> getAdminMessagePage(Integer current, Integer size, String keyword, Integer messageStatus) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionMessage> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            q.like("message_content", keyword.trim());
        }
        if (messageStatus != null) {
            q.eq("message_status", messageStatus);
        }
        q.orderByDesc("create_time");
        List<AuctionMessage> list = list(q);
        return toViewPage(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionMessage addMessage(Long userId, String messageContent, Long goodsId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new RuntimeException("留言内容不能为空");
        }
        String content = messageContent.trim();
        if (content.length() > 500) {
            throw new RuntimeException("留言内容不能超过500字");
        }

        AuctionMessage msg = new AuctionMessage();
        msg.setUserId(userId);
        msg.setGoodsId(goodsId);
        msg.setMessageContent(content);
        msg.setReplyContent(null);
        msg.setReplyTime(null);
        msg.setMessageStatus(0);
        msg.setCreateTime(LocalDateTime.now());
        msg.setDelFlag(0);
        save(msg);
        return msg;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionMessage updateOwnMessage(Long messageId, Long userId, String messageContent) {
        AuctionMessage msg = getById(messageId);
        if (msg == null || msg.getDelFlag() == 1) {
            throw new RuntimeException("留言不存在");
        }
        if (userId == null || !userId.equals(msg.getUserId())) {
            throw new RuntimeException("无权限编辑该留言");
        }
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new RuntimeException("留言内容不能为空");
        }
        String content = messageContent.trim();
        if (content.length() > 500) {
            throw new RuntimeException("留言内容不能超过500字");
        }

        msg.setMessageContent(content);
        // 用户编辑后，原回复失效，重置为未回复
        msg.setReplyContent(null);
        msg.setReplyTime(null);
        msg.setMessageStatus(0);
        updateById(msg);
        return getById(messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOwnMessage(Long messageId, Long userId) {
        AuctionMessage msg = getById(messageId);
        if (msg == null || msg.getDelFlag() == 1) {
            throw new RuntimeException("留言不存在");
        }
        if (userId == null || !userId.equals(msg.getUserId())) {
            throw new RuntimeException("无权限删除该留言");
        }
        msg.setDelFlag(1);
        updateById(msg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionMessage replyMessage(Long messageId, String replyContent) {
        AuctionMessage msg = getById(messageId);
        if (msg == null || msg.getDelFlag() == 1) {
            throw new RuntimeException("留言不存在");
        }
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        String content = replyContent.trim();
        if (content.length() > 500) {
            throw new RuntimeException("回复内容不能超过500字");
        }
        msg.setReplyContent(content);
        msg.setReplyTime(LocalDateTime.now());
        msg.setMessageStatus(1);
        updateById(msg);
        return getById(messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessageAdmin(Long messageId) {
        AuctionMessage msg = getById(messageId);
        if (msg == null || msg.getDelFlag() == 1) {
            throw new RuntimeException("留言不存在");
        }
        msg.setDelFlag(1);
        updateById(msg);
    }

    private PageInfo<Map<String, Object>> toViewPage(List<AuctionMessage> list) {
        PageInfo<AuctionMessage> basePage = new PageInfo<>(list);
        List<Map<String, Object>> viewList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            PageInfo<Map<String, Object>> empty = new PageInfo<>(Collections.emptyList());
            empty.setPageNum(basePage.getPageNum());
            empty.setPageSize(basePage.getPageSize());
            empty.setTotal(basePage.getTotal());
            empty.setPages(basePage.getPages());
            return empty;
        }

        List<Long> userIds = list.stream()
                .map(AuctionMessage::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, AuctionUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<AuctionUser> users = userService.listByIds(userIds);
            for (AuctionUser user : users) {
                if (user != null && user.getDelFlag() == 0) {
                    userMap.put(user.getId(), user);
                }
            }
        }

        for (AuctionMessage m : list) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            row.put("id", m.getId());
            row.put("userId", m.getUserId());
            row.put("goodsId", m.getGoodsId());
            row.put("messageContent", m.getMessageContent());
            row.put("replyContent", m.getReplyContent());
            row.put("replyTime", m.getReplyTime());
            row.put("messageStatus", m.getMessageStatus());
            row.put("createTime", m.getCreateTime());

            AuctionUser user = userMap.get(m.getUserId());
            if (user != null) {
                String name = user.getNickName() != null && !user.getNickName().trim().isEmpty()
                        ? user.getNickName() : user.getUserName();
                row.put("userName", name);
                row.put("userAvatar", resolveAvatar(user));
            } else {
                row.put("userName", "用户");
                row.put("userAvatar", null);
            }
            viewList.add(row);
        }

        PageInfo<Map<String, Object>> page = new PageInfo<>(viewList);
        page.setPageNum(basePage.getPageNum());
        page.setPageSize(basePage.getPageSize());
        page.setTotal(basePage.getTotal());
        page.setPages(basePage.getPages());
        return page;
    }

    private String resolveAvatar(AuctionUser user) {
        if (user.getAvatarFileId() != null) {
            try {
                com.auction.onlineauction.OnlineAuction.entity.AuctionFile file = fileService.getById(user.getAvatarFileId());
                if (file != null && file.getDelFlag() == 0) {
                    return file.getFilePath();
                }
            } catch (Exception ignored) {
            }
        }
        if (user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
            return user.getAvatar();
        }
        return null;
    }
}
