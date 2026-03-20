package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionComplaint;
import com.auction.onlineauction.OnlineAuction.service.IAuctionComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 前台投诉提交接口（买方/卖方）
 */
@RestController
@RequestMapping("/api/complaint")
public class AuctionComplaintController {

    @Autowired
    private IAuctionComplaintService complaintService;

    @PostMapping
    public Result<AuctionComplaint> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("请先登录");
            if (!RoleCheckHelper.canViewMessageBoard(session)) {
                return Result.error("无权限提交投诉");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("请先登录");

            String complaintType = body.get("complaintType") == null ? null : body.get("complaintType").toString();
            Long targetId = body.get("targetId") == null ? null : Long.valueOf(body.get("targetId").toString());
            Long relatedGoodsId = body.get("relatedGoodsId") == null ? null : Long.valueOf(body.get("relatedGoodsId").toString());
            Long relatedOrderId = body.get("relatedOrderId") == null ? null : Long.valueOf(body.get("relatedOrderId").toString());
            Long relatedMessageId = body.get("relatedMessageId") == null ? null : Long.valueOf(body.get("relatedMessageId").toString());
            Long reportedUserId = body.get("reportedUserId") == null ? null : Long.valueOf(body.get("reportedUserId").toString());
            String complaintContent = body.get("complaintContent") == null ? null : body.get("complaintContent").toString();
            String evidence = body.get("evidence") == null ? null : body.get("evidence").toString();

            AuctionComplaint c = complaintService.submitComplaint(
                    userId, complaintType, targetId, relatedGoodsId, relatedOrderId, relatedMessageId, reportedUserId,
                    complaintContent, evidence
            );
            return Result.success("投诉提交成功", c);
        } catch (Exception e) {
            return Result.error("投诉提交失败：" + e.getMessage());
        }
    }
}
