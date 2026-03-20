package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionComplaint;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionComplaintMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionComplaintService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionWorkOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuctionComplaintServiceImpl extends ServiceImpl<AuctionComplaintMapper, AuctionComplaint>
        implements IAuctionComplaintService {

    @Autowired
    private IAuctionWorkOrderService workOrderService;
    private static final Set<String> TYPES = new HashSet<>(
            Arrays.asList("goods", "order", "bid", "message")
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionComplaint submitComplaint(Long userId,
                                            String complaintType,
                                            Long targetId,
                                            Long relatedGoodsId,
                                            Long relatedOrderId,
                                            Long relatedMessageId,
                                            Long reportedUserId,
                                            String complaintContent,
                                            String evidence) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        String type = complaintType == null ? "" : complaintType.trim().toLowerCase();
        if (!TYPES.contains(type)) {
            throw new RuntimeException("投诉类型无效");
        }
        if (targetId == null) {
            throw new RuntimeException("投诉目标不能为空");
        }
        if (complaintContent == null || complaintContent.trim().isEmpty()) {
            throw new RuntimeException("投诉内容不能为空");
        }
        String content = complaintContent.trim();
        if (content.length() > 500) {
            throw new RuntimeException("投诉内容不能超过500字");
        }

        AuctionComplaint c = new AuctionComplaint();
        c.setComplaintUserId(userId);
        c.setComplaintType(type);
        c.setTargetId(targetId);
        c.setRelatedGoodsId(relatedGoodsId);
        c.setRelatedOrderId(relatedOrderId);
        c.setRelatedMessageId(relatedMessageId);
        c.setReportedUserId(reportedUserId);
        c.setComplaintContent(content);
        c.setEvidence(evidence);
        c.setComplaintStatus(0);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        c.setDelFlag(0);
        save(c);

        // 投诉自动生成工单，随机分配客服
        try {
            String title;
            if ("goods".equals(type)) {
                title = "商品投诉 #" + targetId;
            } else if ("order".equals(type)) {
                title = "订单投诉 #" + targetId;
            } else if ("bid".equals(type)) {
                title = "竞价行为投诉 #" + targetId;
            } else if ("message".equals(type)) {
                title = "留言投诉 #" + targetId;
            } else {
                title = "投诉 #" + targetId;
            }
            workOrderService.createWorkOrder(userId, type, targetId, title, content);
        } catch (Exception ignored) {
            // 工单创建失败不影响投诉本身
        }

        return c;
    }
}
