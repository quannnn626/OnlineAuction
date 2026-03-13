package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.*;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMessageCenterMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMessageSessionMapper;
import com.auction.onlineauction.OnlineAuction.service.IMessageCenterService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息中心服务实现（以商品为核心的客服咨询）
 */
@Service
public class MessageCenterServiceImpl implements IMessageCenterService {

    @Autowired
    private AuctionMessageSessionMapper sessionMapper;
    @Autowired
    private AuctionMessageCenterMapper messageMapper;
    @Autowired
    private com.auction.onlineauction.OnlineAuction.service.IAuctionUserService userService;
    @Autowired
    private com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService goodsService;
    @Autowired
    private com.auction.onlineauction.OnlineAuction.service.IAuctionFileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionMessageSession getOrCreateSession(Long goodsId, Long userId) {
        if (goodsId == null || userId == null) {
            throw new RuntimeException("商品ID和用户ID不能为空");
        }
        QueryWrapper<AuctionMessageSession> q = new QueryWrapper<>();
        q.eq("goods_id", goodsId).eq("user_id", userId).eq("del_flag", 0);
        AuctionMessageSession exist = sessionMapper.selectOne(q);
        if (exist != null) {
            if (exist.getSessionStatus() == 1) {
                throw new RuntimeException("该商品会话已关闭，无法继续咨询");
            }
            return exist;
        }
        AuctionGoods goods = goodsService.getById(goodsId);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        Long serviceId = assignRandomService();
        AuctionMessageSession session = new AuctionMessageSession();
        session.setSessionType(1);
        session.setGoodsId(goodsId);
        session.setUserId(userId);
        session.setServiceId(serviceId);
        session.setSessionStatus(0);
        session.setDelFlag(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionMessageSession getOrCreateAdminSession(Long adminUserId, Long targetUserId) {
        if (adminUserId == null || targetUserId == null) {
            throw new RuntimeException("参数不能为空");
        }
        AuctionUser target = userService.getById(targetUserId);
        if (target == null || target.getDelFlag() == 1) {
            throw new RuntimeException("目标用户不存在");
        }
        int targetRole = parseFirstRole(target.getUserRole());
        if (targetRole == 1) {
            throw new RuntimeException("不允许与普通用户建立管理沟通");
        }
        if (targetRole != 2 && targetRole != 5 && targetRole != 6 && targetRole != 7 && targetRole != 8) {
            throw new RuntimeException("仅可与卖方、客服、拍卖师、财务、运营建立管理沟通");
        }
        QueryWrapper<AuctionMessageSession> q = new QueryWrapper<>();
        q.eq("session_type", 2).eq("user_id", adminUserId).eq("service_id", targetUserId).eq("del_flag", 0);
        AuctionMessageSession exist = sessionMapper.selectOne(q);
        if (exist != null) {
            if (exist.getSessionStatus() == 1) {
                throw new RuntimeException("该会话已关闭");
            }
            return exist;
        }
        AuctionMessageSession session = new AuctionMessageSession();
        session.setSessionType(2);
        session.setGoodsId(null);
        session.setUserId(adminUserId);
        session.setServiceId(targetUserId);
        session.setSessionStatus(0);
        session.setDelFlag(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    private int parseFirstRole(String userRole) {
        if (userRole == null || userRole.trim().isEmpty()) return 0;
        String first = userRole.split(",")[0].trim();
        try {
            return Integer.parseInt(first);
        } catch (Exception e) {
            return 0;
        }
    }

    private Long assignRandomService() {
        QueryWrapper<AuctionUser> q = new QueryWrapper<>();
        q.eq("user_status", 0).eq("del_flag", 0);
        q.apply("FIND_IN_SET(6, user_role) > 0");
        List<AuctionUser> list = userService.list(q);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(new Random().nextInt(list.size())).getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionMessageCenter sendMessage(Long sessionId, Long senderId, Integer contentType, String content, Long fileId) {
        AuctionMessageSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getDelFlag() == 1) {
            throw new RuntimeException("会话不存在");
        }
        if (session.getSessionStatus() == 1) {
            throw new RuntimeException("会话已关闭");
        }
        Long receiverId = session.getUserId().equals(senderId) ? session.getServiceId() : session.getUserId();
        if (receiverId == null) {
            throw new RuntimeException("暂无客服分配，请稍后再试");
        }
        AuctionMessageCenter msg = new AuctionMessageCenter();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContentType(contentType != null ? contentType : 1);
        msg.setContent(content);
        msg.setFileId(fileId);
        msg.setIsRead(0);
        msg.setDelFlag(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        return msg;
    }

    @Override
    public PageInfo<Map<String, Object>> getMySessions(Long userId, Integer current, Integer size) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionMessageSession> q = new QueryWrapper<>();
        q.eq("session_type", 1).eq("user_id", userId).eq("del_flag", 0).orderByDesc("update_time");
        List<AuctionMessageSession> list = sessionMapper.selectList(q);
        return toSessionPage(list);
    }

    @Override
    public PageInfo<Map<String, Object>> getServiceSessions(Long serviceId, Integer current, Integer size) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionMessageSession> q = new QueryWrapper<>();
        q.eq("service_id", serviceId).eq("del_flag", 0).in("session_type", 1, 2).orderByDesc("update_time");
        List<AuctionMessageSession> list = sessionMapper.selectList(q);
        return toSessionPage(list);
    }

    @Override
    public PageInfo<Map<String, Object>> getSellerMergedSessions(Long sellerId, Integer current, Integer size) {
        List<AuctionMessageSession> asUser = sessionMapper.selectList(
                new QueryWrapper<AuctionMessageSession>().eq("session_type", 1).eq("user_id", sellerId).eq("del_flag", 0).orderByDesc("update_time"));
        List<AuctionMessageSession> asParticipant = sessionMapper.selectList(
                new QueryWrapper<AuctionMessageSession>().eq("session_type", 2).eq("del_flag", 0)
                        .and(w -> w.eq("user_id", sellerId).or().eq("service_id", sellerId))
                        .orderByDesc("update_time"));
        Set<Long> seen = new HashSet<>();
        List<AuctionMessageSession> merged = new ArrayList<>();
        for (AuctionMessageSession s : asParticipant) {
            if (seen.add(s.getId())) merged.add(s);
        }
        for (AuctionMessageSession s : asUser) {
            if (seen.add(s.getId())) merged.add(s);
        }
        merged.sort((a, b) -> (b.getUpdateTime() == null ? LocalDateTime.MIN : b.getUpdateTime())
                .compareTo(a.getUpdateTime() == null ? LocalDateTime.MIN : a.getUpdateTime()));
        int from = (current - 1) * size;
        int to = Math.min(from + size, merged.size());
        List<AuctionMessageSession> paged = from < merged.size() ? merged.subList(from, to) : Collections.emptyList();
        PageInfo<Map<String, Object>> result = toSessionPage(paged);
        result.setTotal(merged.size());
        result.setPages(size > 0 ? (int) Math.ceil((double) merged.size() / size) : 0);
        result.setPageNum(current);
        result.setPageSize(size);
        return result;
    }

    @Override
    public PageInfo<Map<String, Object>> getAdminSessions(Long currentUserId, Integer current, Integer size, boolean isSuperAdmin) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionMessageSession> q = new QueryWrapper<>();
        q.eq("session_type", 2).eq("del_flag", 0);
        if (!isSuperAdmin) {
            q.and(w -> w.eq("user_id", currentUserId).or().eq("service_id", currentUserId));
        }
        q.orderByDesc("update_time");
        List<AuctionMessageSession> list = sessionMapper.selectList(q);
        return toSessionPage(list);
    }

    @Override
    public PageInfo<Map<String, Object>> getAllSessions(Integer current, Integer size, Long goodsId, Long userId) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionMessageSession> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (goodsId != null) q.eq("goods_id", goodsId);
        if (userId != null) q.eq("user_id", userId);
        q.orderByDesc("update_time");
        List<AuctionMessageSession> list = sessionMapper.selectList(q);
        return toSessionPage(list);
    }

    private PageInfo<Map<String, Object>> toSessionPage(List<AuctionMessageSession> list) {
        PageInfo<AuctionMessageSession> base = new PageInfo<>(list);
        List<Map<String, Object>> rows = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            PageInfo<Map<String, Object>> empty = new PageInfo<>(Collections.emptyList());
            empty.setTotal(base.getTotal());
            empty.setPages(base.getPages());
            empty.setPageNum(base.getPageNum());
            empty.setPageSize(base.getPageSize());
            return empty;
        }
        Set<Long> goodsIds = list.stream().map(AuctionMessageSession::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();
        list.forEach(s -> {
            userIds.add(s.getUserId());
            if (s.getServiceId() != null) userIds.add(s.getServiceId());
        });
        Map<Long, AuctionGoods> goodsMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().filter(g -> g.getDelFlag() == 0).collect(Collectors.toMap(AuctionGoods::getId, g -> g));
        Map<Long, AuctionUser> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userService.listByIds(userIds).stream().filter(u -> u.getDelFlag() == 0).collect(Collectors.toMap(AuctionUser::getId, u -> u));

        for (AuctionMessageSession s : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", s.getId());
            row.put("goodsId", s.getGoodsId());
            row.put("userId", s.getUserId());
            row.put("serviceId", s.getServiceId());
            row.put("sessionStatus", s.getSessionStatus());
            row.put("createTime", s.getCreateTime());
            row.put("updateTime", s.getUpdateTime());
            AuctionGoods g = s.getGoodsId() != null ? goodsMap.get(s.getGoodsId()) : null;
            row.put("goodsName", g != null ? g.getGoodsName() : (s.getSessionType() != null && s.getSessionType() == 2 ? "管理沟通" : "-"));
            row.put("sessionType", s.getSessionType() != null ? s.getSessionType() : 1);
            AuctionUser u = userMap.get(s.getUserId());
            row.put("userName", u != null ? (u.getNickName() != null && !u.getNickName().isEmpty() ? u.getNickName() : u.getUserName()) : "-");
            AuctionUser svc = s.getServiceId() != null ? userMap.get(s.getServiceId()) : null;
            row.put("serviceName", svc != null ? (svc.getNickName() != null && !svc.getNickName().isEmpty() ? svc.getNickName() : svc.getUserName()) : "待分配");
            rows.add(row);
        }
        PageInfo<Map<String, Object>> page = new PageInfo<>(rows);
        page.setTotal(base.getTotal());
        page.setPages(base.getPages());
        page.setPageNum(base.getPageNum());
        page.setPageSize(base.getPageSize());
        return page;
    }

    @Override
    public Map<String, Object> getSessionDetail(Long sessionId, Long currentUserId) {
        AuctionMessageSession s = sessionMapper.selectById(sessionId);
        if (s == null || s.getDelFlag() == 1) {
            throw new RuntimeException("会话不存在");
        }
        if (!canAccessSession(sessionId, currentUserId, false)) {
            throw new RuntimeException("无权限查看该会话");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", s.getId());
        detail.put("goodsId", s.getGoodsId());
        detail.put("userId", s.getUserId());
        detail.put("serviceId", s.getServiceId());
        detail.put("sessionStatus", s.getSessionStatus());
        detail.put("createTime", s.getCreateTime());
        AuctionGoods g = s.getGoodsId() != null ? goodsService.getById(s.getGoodsId()) : null;
        detail.put("goodsName", g != null ? g.getGoodsName() : (s.getSessionType() != null && s.getSessionType() == 2 ? "管理沟通" : "-"));
        detail.put("sessionType", s.getSessionType() != null ? s.getSessionType() : 1);
        AuctionUser u = userService.getById(s.getUserId());
        detail.put("userName", u != null ? (u.getNickName() != null && !u.getNickName().isEmpty() ? u.getNickName() : u.getUserName()) : "-");
        AuctionUser svc = s.getServiceId() != null ? userService.getById(s.getServiceId()) : null;
        detail.put("serviceName", svc != null ? (svc.getNickName() != null && !svc.getNickName().isEmpty() ? svc.getNickName() : svc.getUserName()) : "待分配");
        return detail;
    }

    @Override
    public List<Map<String, Object>> getSessionMessages(Long sessionId, Long currentUserId) {
        AuctionMessageSession s = sessionMapper.selectById(sessionId);
        if (s == null || s.getDelFlag() == 1) {
            throw new RuntimeException("会话不存在");
        }
        if (!canAccessSession(sessionId, currentUserId, false)) {
            throw new RuntimeException("无权限查看该会话");
        }
        QueryWrapper<AuctionMessageCenter> q = new QueryWrapper<>();
        q.eq("session_id", sessionId).eq("del_flag", 0).orderByAsc("create_time");
        List<AuctionMessageCenter> list = messageMapper.selectList(q);
        List<Map<String, Object>> rows = new ArrayList<>();
        Set<Long> fileIds = list.stream().map(AuctionMessageCenter::getFileId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AuctionFile> fileMap = fileIds.isEmpty() ? Collections.emptyMap() :
                fileService.listByIds(fileIds).stream().filter(f -> f.getDelFlag() == 0).collect(Collectors.toMap(AuctionFile::getId, f -> f));
        Set<Long> senderIds = list.stream().map(AuctionMessageCenter::getSenderId).collect(Collectors.toSet());
        Map<Long, AuctionUser> userMap = senderIds.isEmpty() ? Collections.emptyMap() :
                userService.listByIds(senderIds).stream().filter(u -> u.getDelFlag() == 0).collect(Collectors.toMap(AuctionUser::getId, u -> u));

        for (AuctionMessageCenter m : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", m.getId());
            row.put("sessionId", m.getSessionId());
            row.put("senderId", m.getSenderId());
            row.put("receiverId", m.getReceiverId());
            row.put("contentType", m.getContentType());
            row.put("content", m.getContent());
            row.put("fileId", m.getFileId());
            row.put("isRead", m.getIsRead());
            row.put("createTime", m.getCreateTime());
            AuctionUser sender = userMap.get(m.getSenderId());
            row.put("senderName", sender != null ? (sender.getNickName() != null && !sender.getNickName().isEmpty() ? sender.getNickName() : sender.getUserName()) : "-");
            if (m.getFileId() != null) {
                AuctionFile f = fileMap.get(m.getFileId());
                row.put("filePath", f != null ? f.getFilePath() : null);
                row.put("fileName", f != null ? f.getFileName() : null);
                row.put("fileType", f != null ? f.getFileType() : null);
            }
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId, Long readerId) {
        AuctionMessageCenter m = messageMapper.selectById(messageId);
        if (m == null || m.getDelFlag() == 1) return;
        if (!m.getReceiverId().equals(readerId)) return;
        m.setIsRead(1);
        messageMapper.updateById(m);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(Long sessionId, Long operatorId) {
        AuctionMessageSession s = sessionMapper.selectById(sessionId);
        if (s == null || s.getDelFlag() == 1) {
            throw new RuntimeException("会话不存在");
        }
        s.setSessionStatus(1);
        s.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(s);
    }

    @Override
    public boolean canAccessSession(Long sessionId, Long userId, boolean isSuperAdmin) {
        if (userId == null) return false;
        AuctionMessageSession s = sessionMapper.selectById(sessionId);
        if (s == null || s.getDelFlag() == 1) return false;
        int type = s.getSessionType() != null ? s.getSessionType() : 1;
        if (type == 1) {
            if (isSuperAdmin) return false;
            return s.getUserId().equals(userId) || (s.getServiceId() != null && s.getServiceId().equals(userId));
        } else {
            if (isSuperAdmin) return true;
            return s.getUserId().equals(userId) || (s.getServiceId() != null && s.getServiceId().equals(userId));
        }
    }
}
