package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionNotification;
import com.auction.onlineauction.OnlineAuction.entity.AuctionNotificationTarget;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionNotificationMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionNotificationTargetMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionNotificationService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理沟通通知服务实现
 */
@Service
public class AuctionNotificationServiceImpl
        extends ServiceImpl<AuctionNotificationMapper, AuctionNotification>
        implements IAuctionNotificationService {

    @Autowired
    private AuctionNotificationTargetMapper notificationTargetMapper;

    @Autowired
    private IAuctionUserService userService;

    @Override
    @Transactional
    public void sendAdminNotification(Long senderId, List<Long> receiverIds, String title, String content, Integer noticeType, Integer needConfirm) {
        if (senderId == null) {
            throw new RuntimeException("senderId不能为空");
        }
        if (receiverIds == null || receiverIds.isEmpty()) {
            throw new RuntimeException("接收人不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }
        if (noticeType == null) {
            throw new RuntimeException("通知类型不能为空");
        }

        // 去重，避免前端重复勾选导致唯一约束冲突
        receiverIds = receiverIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (receiverIds.isEmpty()) {
            throw new RuntimeException("接收人不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        AuctionNotification notification = new AuctionNotification();
        notification.setSenderId(senderId);
        notification.setNoticeTitle(title.trim());
        notification.setNoticeContent(content.trim());
        notification.setNoticeType(noticeType);
        notification.setNeedConfirm(needConfirm != null ? needConfirm : 0);
        notification.setCreateTime(now);
        notification.setDelFlag(0);

        // 先插入主表拿到 notificationId
        save(notification);
        Long notificationId = notification.getId();
        if (notificationId == null) {
            throw new RuntimeException("通知创建失败：未生成ID");
        }

        // 批量插入接收状态
        List<AuctionNotificationTarget> targets = receiverIds.stream().filter(Objects::nonNull).map(rid -> {
            AuctionNotificationTarget t = new AuctionNotificationTarget();
            t.setNotificationId(notificationId);
            t.setReceiverId(rid);
            t.setIsRead(0);
            t.setIsConfirmed(0);
            t.setReadTime(null);
            t.setConfirmTime(null);
            t.setDelFlag(0);
            return t;
        }).collect(Collectors.toList());

        if (targets.isEmpty()) {
            throw new RuntimeException("接收人不能为空");
        }

        for (AuctionNotificationTarget target : targets) {
            notificationTargetMapper.insert(target);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getSentNotifications(Long senderId, Integer current, Integer size, boolean isSuperAdmin) {
        if (current == null || current <= 0) current = 1;
        if (size == null || size <= 0) size = 10;

        // 分页查询通知主表
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionNotification> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (!isSuperAdmin) {
            q.eq("sender_id", senderId);
        }
        q.orderByDesc("create_time");
        List<AuctionNotification> notifications = list(q);
        PageInfo<AuctionNotification> pageInfo = new PageInfo<>(notifications);

        if (notifications.isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> notificationIds = notifications.stream().map(AuctionNotification::getId).filter(Objects::nonNull).collect(Collectors.toList());
        List<AuctionNotificationTarget> targets = listTargetsByNotificationIds(notificationIds);

        Map<Long, List<AuctionNotificationTarget>> targetGroup = targets.stream()
                .collect(Collectors.groupingBy(AuctionNotificationTarget::getNotificationId));

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AuctionNotification n : notifications) {
            List<AuctionNotificationTarget> group = targetGroup.getOrDefault(n.getId(), Collections.emptyList());
            long total = group.size();
            long readCount = group.stream().filter(t -> t.getIsRead() != null && t.getIsRead() == 1).count();
            long confirmedCount = group.stream().filter(t -> t.getIsConfirmed() != null && t.getIsConfirmed() == 1).count();

            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("senderId", n.getSenderId());
            m.put("noticeTitle", n.getNoticeTitle());
            m.put("noticeType", n.getNoticeType());
            m.put("needConfirm", n.getNeedConfirm());
            m.put("createTime", n.getCreateTime());
            m.put("totalReceivers", total);
            m.put("readCount", readCount);
            m.put("confirmedCount", confirmedCount);
            resultList.add(m);
        }

        PageInfo<Map<String, Object>> resultPage = new PageInfo<>(resultList);
        resultPage.setTotal(pageInfo.getTotal());
        resultPage.setPageNum(pageInfo.getPageNum());
        resultPage.setPageSize(pageInfo.getPageSize());
        return resultPage;
    }

    @Override
    public PageInfo<Map<String, Object>> getInboxNotifications(Long receiverId, Integer current, Integer size) {
        if (receiverId == null) {
            throw new RuntimeException("receiverId不能为空");
        }
        if (current == null || current <= 0) current = 1;
        if (size == null || size <= 0) size = 10;

        // 分页查询接收状态表（再批量查主表拼装展示字段）
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionNotificationTarget> qt = new QueryWrapper<>();
        qt.eq("del_flag", 0);
        qt.eq("receiver_id", receiverId);
        qt.orderByDesc("id");
        List<AuctionNotificationTarget> targets = notificationTargetMapper.selectList(qt);
        PageInfo<AuctionNotificationTarget> pageInfo = new PageInfo<>(targets);

        if (targets.isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> notificationIds = targets.stream().map(AuctionNotificationTarget::getNotificationId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (notificationIds.isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<AuctionNotification> notifications = listByIds(notificationIds);
        Map<Long, AuctionNotification> notificationMap = notifications.stream()
                .collect(Collectors.toMap(AuctionNotification::getId, n -> n));

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AuctionNotificationTarget t : targets) {
            AuctionNotification n = notificationMap.get(t.getNotificationId());
            if (n == null || n.getDelFlag() != null && n.getDelFlag() == 1) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("notificationId", t.getNotificationId());
            m.put("title", n.getNoticeTitle());
            m.put("content", n.getNoticeContent());
            m.put("noticeType", n.getNoticeType());
            m.put("needConfirm", n.getNeedConfirm());
            m.put("createTime", n.getCreateTime());
            m.put("isRead", t.getIsRead());
            m.put("isConfirmed", t.getIsConfirmed());
            m.put("readTime", t.getReadTime());
            m.put("confirmTime", t.getConfirmTime());
            resultList.add(m);
        }

        PageInfo<Map<String, Object>> resultPage = new PageInfo<>(resultList);
        resultPage.setTotal(pageInfo.getTotal());
        resultPage.setPageNum(pageInfo.getPageNum());
        resultPage.setPageSize(pageInfo.getPageSize());
        return resultPage;
    }

    @Override
    @Transactional
    public void confirmReceipt(Long receiverId, Long notificationId) {
        if (receiverId == null || notificationId == null) {
            throw new RuntimeException("receiverId/notificationId不能为空");
        }

        AuctionNotification n = getById(notificationId);
        if (n == null || (n.getDelFlag() != null && n.getDelFlag() == 1)) {
            throw new RuntimeException("通知不存在");
        }

        QueryWrapper<AuctionNotificationTarget> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        q.eq("receiver_id", receiverId);
        q.eq("notification_id", notificationId);
        AuctionNotificationTarget target = notificationTargetMapper.selectOne(q);
        if (target == null) {
            throw new RuntimeException("接收记录不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        target.setIsRead(1);
        target.setReadTime(now);

        // 只要 need_confirm=1，就需要“确认收到”
        if (n.getNeedConfirm() != null && n.getNeedConfirm() == 1) {
            target.setIsConfirmed(1);
            target.setConfirmTime(now);
        }

        notificationTargetMapper.updateById(target);
    }

    @Override
    public List<AuctionNotificationTarget> listTargetsByNotificationIds(List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<AuctionNotificationTarget> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        q.in("notification_id", notificationIds);
        return notificationTargetMapper.selectList(q);
    }

    @Override
    public List<Map<String, Object>> getSentNotificationTargetDetails(Long notificationId, Long senderId, boolean isSuperAdmin) {
        if (notificationId == null) {
            throw new RuntimeException("notificationId不能为空");
        }
        AuctionNotification notification = getById(notificationId);
        if (notification == null || (notification.getDelFlag() != null && notification.getDelFlag() == 1)) {
            throw new RuntimeException("通知不存在");
        }
        if (!isSuperAdmin && (senderId == null || !senderId.equals(notification.getSenderId()))) {
            throw new RuntimeException("无权限查看该通知接收明细");
        }

        QueryWrapper<AuctionNotificationTarget> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        q.eq("notification_id", notificationId);
        q.orderByDesc("id");
        List<AuctionNotificationTarget> targets = notificationTargetMapper.selectList(q);
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (AuctionNotificationTarget t : targets) {
            AuctionUser user = userService.getById(t.getReceiverId());
            Map<String, Object> m = new HashMap<>();
            m.put("receiverId", t.getReceiverId());
            m.put("userName", user != null ? user.getUserName() : null);
            m.put("nickName", user != null ? user.getNickName() : null);
            m.put("userRole", user != null ? user.getUserRole() : null);
            m.put("isRead", t.getIsRead());
            m.put("isConfirmed", t.getIsConfirmed());
            m.put("readTime", t.getReadTime());
            m.put("confirmTime", t.getConfirmTime());
            list.add(m);
        }
        return list;
    }
}

