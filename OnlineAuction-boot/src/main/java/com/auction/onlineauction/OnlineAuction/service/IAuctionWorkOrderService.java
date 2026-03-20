package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionWorkOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.Map;

public interface IAuctionWorkOrderService extends IService<AuctionWorkOrder> {

    AuctionWorkOrder createWorkOrder(Long userId, String workType, Long relatedId, String title, String content);

    PageInfo<Map<String, Object>> getMyWorkOrders(Long userId, Integer current, Integer size);

    PageInfo<Map<String, Object>> getServiceWorkOrders(Long serviceId, Integer current, Integer size, Integer workStatus);

    /** 客服处理：1=处理中 2=提交管理员复核 3=客服直接关闭 */
    void serviceProcessWorkOrder(Long workOrderId,
                                 Long serviceId,
                                 Integer workStatus,
                                 String handleResult,
                                 String penaltyType,
                                 Long penaltyTargetUserId,
                                 BigDecimal penaltyAmount);

    PageInfo<Map<String, Object>> getAdminReviewWorkOrders(Integer current, Integer size, Integer workStatus, String workType);

    /** 管理员复核：通过/驳回（仅 role=3） */
    void adminReviewWorkOrder(Long workOrderId,
                              Long adminId,
                              Boolean approve,
                              String reviewResult,
                              String penaltyType,
                              Long penaltyTargetUserId,
                              BigDecimal penaltyAmount);
}
