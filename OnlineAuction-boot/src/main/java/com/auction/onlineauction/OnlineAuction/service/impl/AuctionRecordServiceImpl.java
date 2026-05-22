package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionRecordMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionDepositService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 竞拍记录表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionRecordServiceImpl extends ServiceImpl<AuctionRecordMapper, AuctionRecord> implements IAuctionRecordService {

    @Autowired
    private AuctionRecordMapper recordMapper;

    @Autowired
    private IAuctionGoodsService goodsService;
    @Autowired
    private IAuctionDepositService depositService;

    @Autowired
    private AuctionRiskBidAttemptLogService riskBidAttemptLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionRecord submitBid(Long goodsId, Long buyerId, BigDecimal bidPrice, String bidIp) {
        // 1. 使用SELECT FOR UPDATE锁定商品记录，保证原子性
        // 这样可以防止多个用户同时出价时出现竞态条件
        AuctionGoods goods = recordMapper.selectGoodsForUpdate(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 2. 验证商品状态
        if (goods.getDelFlag() == 1) {
            throw new RuntimeException("商品已删除");
        }
        if (goods.getAuditStatus() != 1) {
            throw new RuntimeException("商品未审核通过");
        }

        // 3. 验证时间并实时更新商品状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(goods.getStartTime())) {
            throw new RuntimeException("竞拍尚未开始");
        }
        if (now.isAfter(goods.getEndTime())) {
            // 如果已结束，更新商品状态
            goodsService.updateGoodsStatusByTime(goods);
            throw new RuntimeException("竞拍已结束");
        }

        // 4. 如果商品状态不是"竞拍中"，但时间已经到了，则更新状态
        // 这可以处理商品状态还未自动更新的情况
        if (goods.getGoodsStatus() != 1) {
            // 实时更新商品状态
            goodsService.updateGoodsStatusByTime(goods);
            // 重新获取商品信息（因为状态可能已更新）
            goods = recordMapper.selectGoodsForUpdate(goodsId);
            if (goods.getGoodsStatus() != 1) {
                throw new RuntimeException("商品不在竞拍中");
            }
        }

        // 5. 验证出价金额（在锁定状态下验证，保证原子性）
        BigDecimal currentHighestPrice = goods.getCurrentHighestPrice();
        // “当前价格”用于大跳价判定：不存在时退回起拍价
        BigDecimal currentPrice = currentHighestPrice != null ? currentHighestPrice : goods.getBasePrice();
        BigDecimal minBidPrice = currentHighestPrice != null 
            ? currentHighestPrice.add(goods.getAddPrice()) 
            : goods.getBasePrice();
        
        if (bidPrice.compareTo(minBidPrice) < 0) {
            throw new RuntimeException("出价金额不能低于" + minBidPrice + "元");
        }

        // 6. 验证不能是自己发布的商品
        if (goods.getSellerId().equals(buyerId)) {
            throw new RuntimeException("不能竞拍自己发布的商品");
        }

        // ========= 风控实时检测（在冻结保证金/写最高价前执行） =========
        // 规则1：秒速出价（1秒内同一用户同一商品>=3次）-> 拦截拒绝
        long last1sCnt = count(new QueryWrapper<AuctionRecord>()
                .eq("goods_id", goodsId)
                .eq("buyer_id", buyerId)
                .eq("del_flag", 0)
                .ge("bid_time", now.minusSeconds(1)));

        // 规则2：超大额跳价（单次加价>=当前价格50%）-> 拦截拒绝
        boolean bigJumpAbnormal = false;
        if (currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal delta = bidPrice.subtract(currentPrice);
            BigDecimal threshold = currentPrice.multiply(new BigDecimal("0.5"));
            bigJumpAbnormal = delta.compareTo(threshold) >= 0;
        }

        // 规则4：倒计时疯狂加价（最后30秒内同一用户同一商品>=5次）-> 拦截拒绝
        boolean countdownSpamAbnormal = false;
        if (goods.getEndTime() != null) {
            LocalDateTime start = goods.getEndTime().minusSeconds(30);
            if (!now.isBefore(start) && now.isBefore(goods.getEndTime())) {
                long last30sCnt = count(new QueryWrapper<AuctionRecord>()
                        .eq("goods_id", goodsId)
                        .eq("buyer_id", buyerId)
                        .eq("del_flag", 0)
                        .ge("bid_time", start));
                countdownSpamAbnormal = (last30sCnt + 1) >= 5;
            }
        }

        boolean intercept = false;
        String interceptReason = null;
        int interceptRiskRuleType = 0;
        if ((last1sCnt + 1) >= 3) {
            intercept = true;
            interceptReason = "秒速出价（1秒内>=3次）";
            interceptRiskRuleType = 1;
        } else if (bigJumpAbnormal) {
            intercept = true;
            interceptReason = "超大额跳价（单次加价>=当前价格50%）";
            interceptRiskRuleType = 2;
        } else if (countdownSpamAbnormal) {
            intercept = true;
            interceptReason = "倒计时疯狂加价（最后30秒内>=5次）";
            interceptRiskRuleType = 3;
        }

        if (intercept) {
            // 记录被拒绝的出价尝试（用于风控查看）
            try {
                riskBidAttemptLogService.logRejectedBidAttempt(
                        goodsId, buyerId, bidPrice, now, bidIp,
                        1, // abnormal_type：沿用“恶意出价”口径
                        interceptRiskRuleType
                );
            } catch (Exception ignored) {
                // 不影响拦截结果
            }
            throw new RuntimeException("出价被风控拦截：" + interceptReason);
        }

        // 规则3：频繁顶价（同一用户同一商品>=10次）-> 允许出价，标记风险
        long totalBidCntPrev = count(new QueryWrapper<AuctionRecord>()
                .eq("goods_id", goodsId)
                .eq("buyer_id", buyerId)
                .eq("del_flag", 0)
                .eq("bid_status", 1));
        boolean frequentAbnormal = (totalBidCntPrev + 1) >= 10;

        // 规则6：多账号同 IP 竞拍同商品（同一IP下不同账号>=3人）-> 允许出价，标记围标嫌疑
        boolean ringAbnormal = false;
        if (bidIp != null && !bidIp.trim().isEmpty() && !"unknown".equalsIgnoreCase(bidIp.trim())) {
            Long distinctBuyerCnt = recordMapper.countDistinctBuyersByGoodsIdAndBidIp(goodsId, bidIp.trim());
            Long buyerExistCnt = recordMapper.countByGoodsIdBuyerIdAndBidIp(goodsId, buyerId, bidIp.trim());
            long totalDistinct = (buyerExistCnt != null && buyerExistCnt > 0)
                    ? (distinctBuyerCnt != null ? distinctBuyerCnt : 0)
                    : (distinctBuyerCnt != null ? distinctBuyerCnt + 1 : 1);
            ringAbnormal = totalDistinct >= 3;
        }

        // 预警规则：允许出价，标记风险（写入 risk_rule_type 用于区分 5 种异常行为）
        int abnormalType = 0; // abnormal_type：沿用“恶意出价”口径（1=恶意）
        int riskRuleType = 0; // risk_rule_type：1~5 对应你的5种触发规则
        if (frequentAbnormal) {
            abnormalType = 1;
            riskRuleType = 4; // 频繁顶价
        } else if (ringAbnormal) {
            abnormalType = 1;
            riskRuleType = 5; // 围标嫌疑
        }

        // 6.5 首次出价时冻结该商品设定的保证金
        BigDecimal depositRequired = goods.getDepositRequired() != null ? goods.getDepositRequired() : BigDecimal.ZERO;
        if (depositRequired.compareTo(BigDecimal.ZERO) > 0) {
            // 首次冻结应以“已接受出价”作为口径，避免风控拒绝尝试污染保证金冻结逻辑
            long existBidCount = count(new QueryWrapper<AuctionRecord>()
                    .eq("goods_id", goodsId)
                    .eq("buyer_id", buyerId)
                    .eq("del_flag", 0)
                    .eq("bid_status", 1));
            if (existBidCount == 0) {
                depositService.freezeForBid(buyerId, depositRequired, goodsId);
            }
        }

        // 7. 将之前的最高价记录的is_highest设置为0
        // 注意：这里仍然在事务和锁的保护下，保证原子性
        recordMapper.clearHighestFlag(goodsId);

        // 8. 创建新的竞拍记录
        AuctionRecord record = new AuctionRecord();
        record.setGoodsId(goodsId);
        record.setBuyerId(buyerId);
        record.setBidPrice(bidPrice);
        record.setIsAgent(0); // 手动出价
        record.setBidTime(LocalDateTime.now());
        record.setIsHighest(1); // 当前最高价
        record.setAbnormalType(abnormalType);
        record.setBidIp(bidIp);
        record.setBidStatus(1); // 1=已接受出价
        record.setRiskRuleType(riskRuleType);
        record.setDelFlag(0);

        // 9. 保存竞拍记录（在事务中，保证原子性）
        boolean saved = save(record);
        if (!saved) {
            throw new RuntimeException("保存竞拍记录失败");
        }

        // 10. 更新商品的当前最高出价和竞价次数（在事务中，保证原子性）
        // 使用UPDATE语句原子性地更新，避免并发问题
        int updated = recordMapper.updateGoodsHighestPrice(goodsId, bidPrice);
        if (updated == 0) {
            throw new RuntimeException("更新商品最高出价失败");
        }

        // 11. 代理出价自动跟价
        processProxyAutoBidding(goodsId, buyerId, bidPrice, goods.getAddPrice(), bidIp);

        // 12. 返回竞拍记录
        return record;
    }

    /**
     * 代理出价自动跟价——直接计算最终胜者，避免逐次加价的 ping-pong 问题。
     *
     * 算法：将所有有效最高价排序（手动出价 + 各代理出价），最高者胜出，
     * 成交价 = min(胜者有效最高价, 第二名有效最高价 + 加价幅度)。
     */
    private void processProxyAutoBidding(Long goodsId, Long manualBuyerId, BigDecimal manualBidPrice,
                                          BigDecimal addPrice, String triggerBidIp) {
        AuctionGoods goods = recordMapper.selectGoodsForUpdate(goodsId);
        if (goods == null || goods.getCurrentHighestPrice() == null) {
            return;
        }
        BigDecimal currentHighest = goods.getCurrentHighestPrice();

        List<AuctionRecord> proxyBids = recordMapper.selectTopProxyBids(goodsId, currentHighest, 20);
        if (proxyBids.isEmpty()) {
            return;
        }

        // 构建各买家 -> 有效最高价的映射，同时记录 IP
        java.util.Map<Long, BigDecimal> effectiveMaxMap = new java.util.LinkedHashMap<>();
        java.util.Map<Long, String> ipMap = new java.util.HashMap<>();
        for (AuctionRecord proxy : proxyBids) {
            effectiveMaxMap.put(proxy.getBuyerId(), proxy.getAgentMaxPrice());
            ipMap.put(proxy.getBuyerId(), proxy.getBidIp() != null ? proxy.getBidIp() : triggerBidIp);
        }

        // 手动出价方的有效最高价 = max(手动出价金额, 其代理最高价)
        BigDecimal manualEffective = manualBidPrice;
        if (effectiveMaxMap.containsKey(manualBuyerId)) {
            BigDecimal proxyMax = effectiveMaxMap.get(manualBuyerId);
            if (proxyMax.compareTo(manualEffective) > 0) {
                manualEffective = proxyMax;
            }
        }
        effectiveMaxMap.put(manualBuyerId, manualEffective);
        ipMap.putIfAbsent(manualBuyerId, triggerBidIp);

        // 按有效最高价降序排列
        java.util.List<java.util.Map.Entry<Long, BigDecimal>> sorted = new java.util.ArrayList<>(effectiveMaxMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        if (sorted.size() < 2) {
            return;
        }

        long winnerId = sorted.get(0).getKey();
        BigDecimal winnerMax = sorted.get(0).getValue();
        BigDecimal secondMax = sorted.get(1).getValue();

        // 成交价 = min(胜者最高价, 第二名最高价 + 加价幅度)
        BigDecimal newPrice = secondMax.add(addPrice);
        if (newPrice.compareTo(winnerMax) > 0) {
            newPrice = winnerMax;
        }

        // 价格未超过当前最高价，无需操作
        if (newPrice.compareTo(currentHighest) <= 0) {
            return;
        }

        // 胜者就是当前出价人且价格不变
        AuctionRecord currentHighestRecord = recordMapper.selectHighestRecord(goodsId);
        if (currentHighestRecord != null
                && currentHighestRecord.getBuyerId().equals(winnerId)
                && currentHighestRecord.getBidPrice().compareTo(newPrice) == 0) {
            return;
        }

        // 创建代理自动出价记录
        recordMapper.clearHighestFlag(goodsId);

        AuctionRecord autoRecord = new AuctionRecord();
        autoRecord.setGoodsId(goodsId);
        autoRecord.setBuyerId(winnerId);
        autoRecord.setBidPrice(newPrice);
        autoRecord.setIsAgent(1);
        autoRecord.setAgentMaxPrice(winnerMax);
        autoRecord.setBidTime(LocalDateTime.now());
        autoRecord.setIsHighest(1);
        autoRecord.setBidIp(ipMap.getOrDefault(winnerId, triggerBidIp));
        autoRecord.setBidStatus(1);
        autoRecord.setDelFlag(0);
        save(autoRecord);

        recordMapper.updateGoodsHighestPrice(goodsId, newPrice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionRecord submitProxyBid(Long goodsId, Long buyerId, BigDecimal agentMaxPrice, String bidIp) {
        AuctionGoods goods = recordMapper.selectGoodsForUpdate(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (goods.getDelFlag() == 1) {
            throw new RuntimeException("商品已删除");
        }
        if (goods.getAuditStatus() != 1) {
            throw new RuntimeException("商品未审核通过");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(goods.getStartTime())) {
            throw new RuntimeException("竞拍尚未开始");
        }
        if (now.isAfter(goods.getEndTime())) {
            goodsService.updateGoodsStatusByTime(goods);
            throw new RuntimeException("竞拍已结束");
        }
        if (goods.getGoodsStatus() != 1) {
            goodsService.updateGoodsStatusByTime(goods);
            goods = recordMapper.selectGoodsForUpdate(goodsId);
            if (goods.getGoodsStatus() != 1) {
                throw new RuntimeException("商品不在竞拍中");
            }
        }
        if (goods.getSellerId().equals(buyerId)) {
            throw new RuntimeException("不能竞拍自己发布的商品");
        }

        BigDecimal minBidPrice = goods.getCurrentHighestPrice() != null
                ? goods.getCurrentHighestPrice().add(goods.getAddPrice())
                : goods.getBasePrice();

        if (agentMaxPrice.compareTo(minBidPrice) < 0) {
            throw new RuntimeException("代理最高价不能低于" + minBidPrice + "元");
        }

        // 检查是否已有有效代理出价
        int activeCount = recordMapper.countActiveProxyByGoodsAndBuyer(goodsId, buyerId);
        if (activeCount > 0) {
            throw new RuntimeException("您已设置代理出价，请等待当前代理出价被超过后再重新设置");
        }

        // 冻结保证金（首次出价时）
        BigDecimal depositRequired = goods.getDepositRequired() != null ? goods.getDepositRequired() : BigDecimal.ZERO;
        if (depositRequired.compareTo(BigDecimal.ZERO) > 0) {
            long existBidCount = count(new QueryWrapper<AuctionRecord>()
                    .eq("goods_id", goodsId)
                    .eq("buyer_id", buyerId)
                    .eq("del_flag", 0)
                    .eq("bid_status", 1));
            if (existBidCount == 0) {
                depositService.freezeForBid(buyerId, depositRequired, goodsId);
            }
        }

        recordMapper.clearHighestFlag(goodsId);

        AuctionRecord record = new AuctionRecord();
        record.setGoodsId(goodsId);
        record.setBuyerId(buyerId);
        record.setBidPrice(minBidPrice);
        record.setIsAgent(1);
        record.setAgentMaxPrice(agentMaxPrice);
        record.setBidTime(now);
        record.setIsHighest(1);
        record.setBidIp(bidIp);
        record.setBidStatus(1);
        record.setDelFlag(0);
        save(record);

        recordMapper.updateGoodsHighestPrice(goodsId, minBidPrice);

        // 代理出价后也可能触发其他代理出价的跟价
        processProxyAutoBidding(goodsId, buyerId, minBidPrice, goods.getAddPrice(), bidIp);

        return record;
    }

    @Override
    public List<AuctionRecord> getRecordsByGoodsId(Long goodsId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return recordMapper.selectRecordsByGoodsId(goodsId, limit);
    }

    @Override
    public AuctionRecord getHighestRecordByGoodsId(Long goodsId) {
        if (goodsId == null) {
            return null;
        }
        return recordMapper.selectHighestRecord(goodsId);
    }

    @Override
    public PageInfo<Map<String, Object>> getMyBidGoodsPage(Integer current, Integer size, Long buyerId, String keyword) {
        PageHelper.startPage(current, size);
        List<Map<String, Object>> rows = recordMapper.selectMyBidGoodsPage(buyerId, keyword);
        return new PageInfo<>(rows);
    }

    @Override
    public PageInfo<Map<String, Object>> getAdminBidGoodsPage(Integer current, Integer size, String keyword) {
        PageHelper.startPage(current, size);
        List<Map<String, Object>> rows = recordMapper.selectAdminBidGoodsPage(keyword);
        return new PageInfo<>(rows);
    }

    @Override
    public PageInfo<AuctionRecord> getBidRecordsByGoodsPage(Integer current, Integer size, Long goodsId) {
        PageHelper.startPage(current, size);
        List<AuctionRecord> records = recordMapper.selectRecordsPageByGoodsId(goodsId);
        return new PageInfo<>(records);
    }

    @Override
    public PageInfo<AuctionRecord> getMyBidRecordsByGoodsPage(Integer current, Integer size, Long goodsId, Long buyerId) {
        PageHelper.startPage(current, size);
        List<AuctionRecord> records = recordMapper.selectMyRecordsPageByGoodsId(goodsId, buyerId);
        return new PageInfo<>(records);
    }

    @Override
    public void markAbnormal(Long recordId, Integer abnormalType) {
        if (abnormalType == null || (abnormalType != 0 && abnormalType != 1 && abnormalType != 2)) {
            throw new RuntimeException("异常类型无效（0=正常 1=恶意出价 2=机器人）");
        }
        AuctionRecord record = getById(recordId);
        if (record == null || record.getDelFlag() == 1) {
            throw new RuntimeException("竞拍记录不存在");
        }
        record.setAbnormalType(abnormalType);
        updateById(record);
    }

    @Override
    public PageInfo<AuctionRecord> getRecordsByBuyerIdPage(Integer current, Integer size, Long buyerId) {
        if (buyerId == null) {
            return new PageInfo<>(java.util.Collections.emptyList());
        }
        PageHelper.startPage(current, size);
        List<AuctionRecord> list = recordMapper.selectRecordsByBuyerIdPage(buyerId);
        return new PageInfo<>(list);
    }
}
