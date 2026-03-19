package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.entity.AuctionSpecialGoods;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionGoodsMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionSpecialGoodsMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionSpecialService;
import com.auction.onlineauction.OnlineAuction.entity.AuctionSpecial;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionSpecialMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuctionSpecialServiceImpl extends ServiceImpl<AuctionSpecialMapper, AuctionSpecial> implements IAuctionSpecialService {

    @Autowired
    private AuctionSpecialGoodsMapper specialGoodsMapper;
    @Autowired
    private AuctionGoodsMapper goodsMapper;
    @Autowired
    private IAuctionFileService fileService;

    @Override
    public List<Map<String, Object>> listGoodsBySpecialId(Long specialId) {
        QueryWrapper<AuctionSpecialGoods> q = new QueryWrapper<>();
        q.eq("special_id", specialId).orderByAsc("sort_order");
        List<AuctionSpecialGoods> list = specialGoodsMapper.selectList(q);
        if (list.isEmpty()) return new ArrayList<>();
        List<Long> goodsIds = list.stream().map(AuctionSpecialGoods::getGoodsId).distinct().collect(Collectors.toList());
        List<AuctionGoods> goodsList = goodsMapper.selectBatchIds(goodsIds);
        // 仅返回有效、已上架、审核通过的商品（避免前台看到下架/删除/未过审商品）
        goodsList = goodsList.stream()
                .filter(g -> g != null
                        && (g.getDelFlag() == null || g.getDelFlag() == 0)
                        && (g.getShelfStatus() == null || g.getShelfStatus() == 1)
                        && (g.getAuditStatus() == null || g.getAuditStatus() == 1))
                .collect(Collectors.toList());
        Map<Long, AuctionGoods> goodsMap = goodsList.stream().collect(Collectors.toMap(AuctionGoods::getId, g -> g));
        List<Map<String, Object>> result = new ArrayList<>();
        for (AuctionSpecialGoods sg : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", sg.getId());
            row.put("goodsId", sg.getGoodsId());
            row.put("sortOrder", sg.getSortOrder());
            AuctionGoods g = goodsMap.get(sg.getGoodsId());
            if (g != null) {
                // 为前台专场页提供展示需要的核心字段（保持向后兼容：admin 仍可用 goodsId/goodsName/sortOrder）
                row.put("goodsName", g.getGoodsName());
                row.put("goodsDesc", g.getGoodsDesc());
                row.put("basePrice", g.getBasePrice());
                row.put("currentHighestPrice", g.getCurrentHighestPrice());
                row.put("startTime", g.getStartTime());
                row.put("endTime", g.getEndTime());
                row.put("goodsStatus", g.getGoodsStatus());
                row.put("fileIds", g.getFileIds());
                // 加载文件列表（用于图片展示）
                String fileIds = g.getFileIds();
                if (fileIds != null && !fileIds.trim().isEmpty()) {
                    List<AuctionFile> files = fileService.getFilesByIds(fileIds);
                    row.put("files", files);
                } else {
                    row.put("files", new ArrayList<>());
                }
            } else {
                // 商品不存在/被过滤时，不返回空壳数据
                continue;
            }
            result.add(row);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGoods(Long specialId, Long goodsId, Integer sortOrder) {
        QueryWrapper<AuctionSpecialGoods> q = new QueryWrapper<>();
        q.eq("special_id", specialId).eq("goods_id", goodsId);
        if (specialGoodsMapper.selectCount(q) > 0) return;
        AuctionSpecialGoods sg = new AuctionSpecialGoods();
        sg.setSpecialId(specialId);
        sg.setGoodsId(goodsId);
        sg.setSortOrder(sortOrder != null ? sortOrder : 0);
        specialGoodsMapper.insert(sg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGoods(Long specialId, Long goodsId) {
        QueryWrapper<AuctionSpecialGoods> q = new QueryWrapper<>();
        q.eq("special_id", specialId).eq("goods_id", goodsId);
        specialGoodsMapper.delete(q);
    }
}
