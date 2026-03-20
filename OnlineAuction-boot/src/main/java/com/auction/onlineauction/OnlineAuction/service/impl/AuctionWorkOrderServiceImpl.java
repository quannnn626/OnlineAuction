package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageCenter;
import com.auction.onlineauction.OnlineAuction.entity.AuctionMessageSession;
import com.auction.onlineauction.OnlineAuction.entity.AuctionWorkOrder;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMessageCenterMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionMessageSessionMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionWorkOrderMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionWorkOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuctionWorkOrderServiceImpl extends ServiceImpl<AuctionWorkOrderMapper, AuctionWorkOrder>
        implements IAuctionWorkOrderService {

    /** 与会话分配保持一致：30分钟内登录视为在线 */
    private static final int ONLINE_MINUTES = 30;

    @Autowired
    private IAuctionUserService userService;
    @Autowired
    private IAuctionDepositService depositService;
    @Autowired
    private AuctionMessageSessionMapper sessionMapper;
    @Autowired
    private AuctionMessageCenterMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionWorkOrder createWorkOrder(Long userId, String workType, Long relatedId, String title, String content) {
        if (userId == null) throw new RuntimeException("请先登录");
        String type = workType == null ? "" : workType.trim().toLowerCase();
        if (!Arrays.asList("goods", "order", "bid", "message", "other").contains(type)) {
            throw new RuntimeException("工单类型无效");
        }
        if (title == null || title.trim().isEmpty()) throw new RuntimeException("工单标题不能为空");
        if (title.trim().length() > 100) throw new RuntimeException("工单标题不能超过100字");
        if (content == null || content.trim().isEmpty()) throw new RuntimeException("工单内容不能为空");
        if (content.trim().length() > 1000) throw new RuntimeException("工单内容不能超过1000字");

        Long serviceId = assignServiceByLoadBalance();
        if (serviceId == null) throw new RuntimeException("暂无可用客服，请稍后重试");

        AuctionWorkOrder w = new AuctionWorkOrder();
        w.setWorkNo(genWorkNo());
        w.setUserId(userId);
        w.setServiceId(serviceId);
        w.setWorkType(type);
        w.setRelatedId(relatedId);
        w.setTitle(title.trim());
        w.setContent(content.trim());
        w.setWorkStatus(0); // 待处理
        w.setHandleResult(null);
        w.setHandleTime(null);
        w.setCreateTime(LocalDateTime.now());
        w.setUpdateTime(LocalDateTime.now());
        w.setDelFlag(0);
        save(w);
        return w;
    }

    @Override
    public PageInfo<Map<String, Object>> getMyWorkOrders(Long userId, Integer current, Integer size) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionWorkOrder> q = new QueryWrapper<>();
        q.eq("del_flag", 0).eq("user_id", userId).orderByDesc("create_time");
        return toViewPage(list(q));
    }

    @Override
    public PageInfo<Map<String, Object>> getServiceWorkOrders(Long serviceId, Integer current, Integer size, Integer workStatus) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionWorkOrder> q = new QueryWrapper<>();
        q.eq("del_flag", 0).eq("service_id", serviceId);
        if (workStatus != null) q.eq("work_status", workStatus);
        q.orderByAsc("work_status").orderByDesc("create_time");
        return toViewPage(list(q));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void serviceProcessWorkOrder(Long workOrderId,
                                        Long serviceId,
                                        Integer workStatus,
                                        String handleResult,
                                        String penaltyType,
                                        Long penaltyTargetUserId,
                                        BigDecimal penaltyAmount) {
        AuctionWorkOrder w = getById(workOrderId);
        if (w == null || w.getDelFlag() == 1) throw new RuntimeException("工单不存在");
        if (serviceId == null || !serviceId.equals(w.getServiceId())) throw new RuntimeException("无权限处理该工单");
        if (workStatus == null || (workStatus != 1 && workStatus != 2 && workStatus != 3)) {
            throw new RuntimeException("状态仅支持处理中(1)、提交复核(2)、客服关闭(3)");
        }
        if ((workStatus == 2 || workStatus == 3) && (handleResult == null || handleResult.trim().isEmpty())) {
            throw new RuntimeException("请填写处理意见");
        }
        String resultText = handleResult == null ? null : handleResult.trim();
        w.setWorkStatus(workStatus);
        w.setHandleResult(resultText);
        if (workStatus == 2) {
            // 提交管理员复核：记录建议处罚
            w.setPenaltyType(normalizePenaltyType(penaltyType));
            w.setPenaltyTargetUserId(penaltyTargetUserId);
            w.setPenaltyAmount(penaltyAmount);
        }
        if (workStatus == 3) {
            // 客服直接关闭
            w.setHandleTime(LocalDateTime.now());
        }
        w.setUpdateTime(LocalDateTime.now());
        updateById(w);

        // 客服直接关闭 -> 通知投诉人
        if (workStatus == 3) {
            notifyUserByMessageCenter(w, serviceId, "客服处理完成（无需管理员复核）：" + resultText);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getAdminReviewWorkOrders(Integer current, Integer size, Integer workStatus, String workType) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionWorkOrder> q = new QueryWrapper<>();
        q.eq("del_flag", 0);
        if (workStatus != null) q.eq("work_status", workStatus);
        if (workType != null && !workType.trim().isEmpty()) q.eq("work_type", workType.trim().toLowerCase());
        q.orderByDesc("update_time").orderByDesc("id");
        return toViewPage(list(q));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminReviewWorkOrder(Long workOrderId,
                                     Long adminId,
                                     Boolean approve,
                                     String reviewResult,
                                     String penaltyType,
                                     Long penaltyTargetUserId,
                                     BigDecimal penaltyAmount) {
        AuctionWorkOrder w = getById(workOrderId);
        if (w == null || w.getDelFlag() == 1) throw new RuntimeException("工单不存在");
        if (w.getWorkStatus() == null || w.getWorkStatus() != 2) throw new RuntimeException("该工单不在待复核状态");
        if (approve == null) throw new RuntimeException("请指定复核结果");
        if (reviewResult == null || reviewResult.trim().isEmpty()) throw new RuntimeException("请填写复核意见");

        w.setReviewAdminId(adminId);
        w.setReviewTime(LocalDateTime.now());
        w.setReviewResult(reviewResult.trim());

        if (approve) {
            String pType = normalizePenaltyType(penaltyType != null ? penaltyType : w.getPenaltyType());
            Long targetUserId = penaltyTargetUserId != null ? penaltyTargetUserId : w.getPenaltyTargetUserId();
            BigDecimal amount = penaltyAmount != null ? penaltyAmount : w.getPenaltyAmount();
            executePenalty(pType, targetUserId, amount, w);
            w.setPenaltyType(pType);
            w.setPenaltyTargetUserId(targetUserId);
            w.setPenaltyAmount(amount);
            w.setWorkStatus(3); // 已完成
            w.setHandleTime(LocalDateTime.now());
            w.setUpdateTime(LocalDateTime.now());
            updateById(w);
            notifyUserByMessageCenter(w, w.getServiceId(), "管理员复核通过，处理结果：" + w.getReviewResult());
        } else {
            w.setWorkStatus(4); // 驳回
            w.setHandleTime(LocalDateTime.now());
            w.setUpdateTime(LocalDateTime.now());
            updateById(w);
            notifyUserByMessageCenter(w, w.getServiceId(), "管理员复核驳回，不执行处罚。说明：" + w.getReviewResult());
        }
    }

    private void notifyUserByMessageCenter(AuctionWorkOrder w, Long serviceId, String resultText) {
        if (w == null || w.getUserId() == null || serviceId == null) return;
        QueryWrapper<AuctionMessageSession> sq = new QueryWrapper<>();
        sq.eq("session_type", 1)
                .eq("user_id", w.getUserId())
                .eq("service_id", serviceId)
                .eq("del_flag", 0)
                .orderByDesc("update_time")
                .last("limit 1");
        AuctionMessageSession session = sessionMapper.selectOne(sq);
        if (session == null) {
            session = new AuctionMessageSession();
            session.setSessionType(1);
            // 若工单关联的是商品/竞拍，尽量带上商品ID，方便上下文定位
            if ("goods".equals(w.getWorkType()) || "bid".equals(w.getWorkType())) {
                session.setGoodsId(w.getRelatedId());
            } else {
                session.setGoodsId(null);
            }
            session.setUserId(w.getUserId());
            session.setServiceId(serviceId);
            session.setSessionStatus(0);
            session.setDelFlag(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.insert(session);
        } else if (session.getSessionStatus() != null && session.getSessionStatus() == 1) {
            // 若历史会话已关闭，自动恢复为进行中，确保用户可看到通知
            session.setSessionStatus(0);
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }

        AuctionMessageCenter msg = new AuctionMessageCenter();
        msg.setSessionId(session.getId());
        msg.setSenderId(serviceId);
        msg.setReceiverId(w.getUserId());
        msg.setContentType(1);
        msg.setContent(buildWorkOrderDoneText(w, resultText));
        msg.setFileId(null);
        msg.setIsRead(0);
        msg.setDelFlag(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);

        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);
    }

    private String normalizePenaltyType(String penaltyType) {
        String p = penaltyType == null ? "NONE" : penaltyType.trim().toUpperCase();
        if (!Arrays.asList("NONE", "WARN", "DEDUCT_DEPOSIT", "BAN_USER").contains(p)) {
            return "NONE";
        }
        return p;
    }

    private void executePenalty(String penaltyType, Long targetUserId, BigDecimal penaltyAmount, AuctionWorkOrder w) {
        if ("NONE".equals(penaltyType) || "WARN".equals(penaltyType)) return;
        if (targetUserId == null) throw new RuntimeException("处罚目标用户不能为空");
        if ("DEDUCT_DEPOSIT".equals(penaltyType)) {
            if (penaltyAmount == null || penaltyAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("扣保证金金额必须大于0");
            }
            String remark = "工单处罚扣保证金，工单号：" + w.getWorkNo();
            depositService.deductForDefault(targetUserId, penaltyAmount, null, remark);
            return;
        }
        if ("BAN_USER".equals(penaltyType)) {
            AuctionUser u = userService.getById(targetUserId);
            if (u == null || u.getDelFlag() == 1) throw new RuntimeException("目标用户不存在");
            u.setUserStatus(1); // 禁用账号
            userService.updateById(u);
        }
    }

    private String buildWorkOrderDoneText(AuctionWorkOrder w, String resultText) {
        String result = (resultText == null || resultText.isEmpty()) ? "已处理完成" : resultText;
        return "【工单处理完成】工单号：" + w.getWorkNo() + "，处理结果：" + result;
    }

    /**
     * 复用会话分配策略：在线优先 + 未完成工单最少 + 同量随机
     */
    private Long assignServiceByLoadBalance() {
        QueryWrapper<AuctionUser> q = new QueryWrapper<>();
        q.eq("user_status", 0).eq("del_flag", 0);
        q.apply("FIND_IN_SET(6, user_role) > 0");
        q.ge("login_date", LocalDateTime.now().minusMinutes(ONLINE_MINUTES));
        List<AuctionUser> list = userService.list(q);
        if (list == null || list.isEmpty()) {
            q = new QueryWrapper<>();
            q.eq("user_status", 0).eq("del_flag", 0);
            q.apply("FIND_IN_SET(6, user_role) > 0");
            list = userService.list(q);
        }
        if (list == null || list.isEmpty()) return null;

        long minCount = Long.MAX_VALUE;
        List<Long> candidates = new ArrayList<>();
        for (AuctionUser u : list) {
            QueryWrapper<AuctionWorkOrder> sq = new QueryWrapper<>();
            sq.eq("service_id", u.getId()).eq("del_flag", 0).in("work_status", 0, 1);
            long cnt = count(sq);
            if (cnt < minCount) {
                minCount = cnt;
                candidates.clear();
                candidates.add(u.getId());
            } else if (cnt == minCount) {
                candidates.add(u.getId());
            }
        }
        return candidates.get(new Random().nextInt(candidates.size()));
    }

    private String genWorkNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = 1000 + new Random().nextInt(9000);
        return "WO" + ts + rnd;
    }

    private PageInfo<Map<String, Object>> toViewPage(List<AuctionWorkOrder> list) {
        PageInfo<AuctionWorkOrder> base = new PageInfo<>(list);
        if (list == null || list.isEmpty()) {
            PageInfo<Map<String, Object>> empty = new PageInfo<>(Collections.emptyList());
            empty.setTotal(base.getTotal());
            empty.setPages(base.getPages());
            empty.setPageNum(base.getPageNum());
            empty.setPageSize(base.getPageSize());
            return empty;
        }
        Set<Long> userIds = new HashSet<>();
        list.forEach(w -> {
            userIds.add(w.getUserId());
            if (w.getServiceId() != null) userIds.add(w.getServiceId());
        });
        Map<Long, AuctionUser> userMap = userService.listByIds(userIds).stream()
                .filter(u -> u.getDelFlag() == 0)
                .collect(Collectors.toMap(AuctionUser::getId, u -> u, (a, b) -> a));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AuctionWorkOrder w : list) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            row.put("id", w.getId());
            row.put("workNo", w.getWorkNo());
            row.put("workType", w.getWorkType());
            row.put("relatedId", w.getRelatedId());
            row.put("title", w.getTitle());
            row.put("content", w.getContent());
            row.put("workStatus", w.getWorkStatus());
            row.put("handleResult", w.getHandleResult());
            row.put("createTime", w.getCreateTime());
            row.put("updateTime", w.getUpdateTime());
            row.put("userId", w.getUserId());
            row.put("serviceId", w.getServiceId());
            AuctionUser u = userMap.get(w.getUserId());
            AuctionUser s = userMap.get(w.getServiceId());
            row.put("userName", u == null ? "用户" : ((u.getNickName() != null && !u.getNickName().trim().isEmpty()) ? u.getNickName() : u.getUserName()));
            row.put("serviceName", s == null ? null : ((s.getNickName() != null && !s.getNickName().trim().isEmpty()) ? s.getNickName() : s.getUserName()));
            rows.add(row);
        }
        PageInfo<Map<String, Object>> p = new PageInfo<>(rows);
        p.setTotal(base.getTotal());
        p.setPages(base.getPages());
        p.setPageNum(base.getPageNum());
        p.setPageSize(base.getPageSize());
        return p;
    }
}
