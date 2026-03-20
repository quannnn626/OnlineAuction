package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控：记录“被拒绝的出价尝试”用于后续审计/查看。
 * <p>
 * 采用 REQUIRES_NEW，确保即使提交竞拍主事务回滚，日志仍然落库。
 */
@Service
public class AuctionRiskBidAttemptLogService {

    @Autowired
    private AuctionRecordMapper recordMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void logRejectedBidAttempt(Long goodsId,
                                        Long buyerId,
                                        BigDecimal bidPrice,
                                        LocalDateTime bidTime,
                                        String bidIp,
                                        Integer abnormalType,
                                        Integer riskRuleType) {
        AuctionRecord record = new AuctionRecord();
        record.setGoodsId(goodsId);
        record.setBuyerId(buyerId);
        record.setBidPrice(bidPrice);
        record.setIsAgent(0);
        record.setBidTime(bidTime != null ? bidTime : LocalDateTime.now());
        record.setIsHighest(0);
        record.setAbnormalType(abnormalType != null ? abnormalType : 1);
        record.setRiskRuleType(riskRuleType != null ? riskRuleType : 0);
        record.setBidIp(bidIp);
        record.setBidStatus(0); // 0=拒绝出价尝试
        record.setDelFlag(0);
        recordMapper.insert(record);
    }
}

