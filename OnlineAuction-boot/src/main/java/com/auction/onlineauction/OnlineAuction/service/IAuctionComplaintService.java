package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionComplaint;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IAuctionComplaintService extends IService<AuctionComplaint> {

    AuctionComplaint submitComplaint(Long userId,
                                     String complaintType,
                                     Long targetId,
                                     Long relatedGoodsId,
                                     Long relatedOrderId,
                                     Long relatedMessageId,
                                     Long reportedUserId,
                                     String complaintContent,
                                     String evidence);
}
