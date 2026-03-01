package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionRecordMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionRecordService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionRecord submitBid(Long goodsId, Long buyerId, BigDecimal bidPrice) {
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

        // 11. 返回竞拍记录
        // 注意：整个方法在@Transactional和SELECT FOR UPDATE的保护下，
        // 确保多个用户同时出价时不会出现价格混乱或数据不一致的问题
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
}
