package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionGoodsMapper;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionRecordMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 拍卖商品表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionGoodsServiceImpl extends ServiceImpl<AuctionGoodsMapper, AuctionGoods> implements IAuctionGoodsService {

    @Autowired
    private IAuctionFileService fileService;

    @Autowired
    private AuctionRecordMapper recordMapper;

    @Autowired
    private IAuctionOrderService orderService;

    @Override
    public PageInfo<AuctionGoods> getGoodsPage(Integer current, Integer size, String goodsName, String categoryId, Integer auditStatus, Integer goodsStatus) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        if (goodsName != null && !goodsName.trim().isEmpty()) {
            wrapper.like("goods_name", goodsName);
        }
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            // 支持多分类查询（categoryId可能是逗号分隔的字符串）
            wrapper.apply("FIND_IN_SET({0}, category_id)", categoryId);
        }
        if (auditStatus != null) {
            wrapper.eq("audit_status", auditStatus);
        }
        if (goodsStatus != null) {
            wrapper.eq("goods_status", goodsStatus);
        }
        wrapper.orderByDesc("create_time");

        List<AuctionGoods> list = list(wrapper);

        // 为每个商品加载文件信息和自动更新状态
        for (AuctionGoods goods : list) {
            updateGoodsStatusByTime(goods);
            loadFilesForGoods(goods);
        }

        return new PageInfo<>(list);
    }

    @Override
    public List<AuctionGoods> getGoodsList() {
        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.orderByDesc("create_time");
        List<AuctionGoods> list = list(wrapper);

        // 为每个商品加载文件信息
        for (AuctionGoods goods : list) {
            loadFilesForGoods(goods);
        }

        return list;
    }

    @Override
    public AuctionGoods getGoodsById(Long id) {
        AuctionGoods goods = getById(id);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        updateGoodsStatusByTime(goods);
        loadFilesForGoods(goods);
        return goods;
    }

    @Override
    public AuctionGoods getGoodsByIdForPublic(Long id) {
        AuctionGoods goods = getById(id);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        if (goods.getAuditStatus() == null || goods.getAuditStatus() != 1) {
            throw new RuntimeException("商品未通过审核");
        }
        if (goods.getShelfStatus() == null || goods.getShelfStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        updateGoodsStatusByTime(goods);
        loadFilesForGoods(goods);
        return goods;
    }

    @Override
    public AuctionGoods addGoods(Map<String, Object> requestData, Long sellerId) {
        // 将Map转换为AuctionGoods对象
        AuctionGoods goods = convertMapToGoods(requestData);
        
        // 验证商品名称
        if (goods.getGoodsName() == null || goods.getGoodsName().trim().isEmpty()) {
            throw new RuntimeException("商品名称不能为空");
        }
        
        // 设置默认值
        if (goods.getAuditStatus() == null) {
            goods.setAuditStatus(0); // 待审核
        }
        if (goods.getGoodsStatus() == null) {
            goods.setGoodsStatus(0); // 未开始
        }
        if (goods.getShelfStatus() == null) {
            goods.setShelfStatus(0); // 默认下架
        }
        if (sellerId == null) {
            throw new RuntimeException("发布用户不能为空");
        }
        goods.setSellerId(sellerId);
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goods.setDelFlag(0);
        
        boolean success = save(goods);
        if (!success) {
            throw new RuntimeException("新增失败");
        }
        
        // 处理文件关联
        processFileIds(goods.getId(), requestData);
        
        return goods;
    }

    @Override
    public AuctionGoods updateGoods(Long id, Map<String, Object> requestData) {
        // 验证商品是否存在
        AuctionGoods existing = getById(id);
        if (existing == null || existing.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        
        // 将Map转换为AuctionGoods对象
        AuctionGoods goods = convertMapToGoods(requestData);
        goods.setId(id);
        
        // 验证商品名称
        if (goods.getGoodsName() == null || goods.getGoodsName().trim().isEmpty()) {
            throw new RuntimeException("商品名称不能为空");
        }
        
        // 设置更新信息
        goods.setUpdateTime(LocalDateTime.now());
        // 保留创建时间、删除标志、审核状态、商品状态、卖方ID等
        goods.setCreateTime(existing.getCreateTime());
        goods.setDelFlag(existing.getDelFlag());
        if (goods.getAuditStatus() == null) {
            goods.setAuditStatus(existing.getAuditStatus());
        }
        if (goods.getGoodsStatus() == null) {
            goods.setGoodsStatus(existing.getGoodsStatus());
        }
        if (goods.getShelfStatus() == null) {
            goods.setShelfStatus(existing.getShelfStatus());
        }
        if (goods.getSellerId() == null) {
            goods.setSellerId(existing.getSellerId());
        }
        
        boolean success = updateById(goods);
        if (!success) {
            throw new RuntimeException("更新失败");
        }
        
        // 处理文件关联更新
        processFileIdsForUpdate(id, requestData);
        
        return goods;
    }

    @Override
    public void deleteGoods(Long id) {
        AuctionGoods goods = getById(id);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        // 逻辑删除
        goods.setDelFlag(1);
        goods.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(goods);
        if (!success) {
            throw new RuntimeException("删除失败");
        }
    }

    @Override
    public void batchDeleteGoods(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择要删除的商品");
        }
        for (Long id : ids) {
            AuctionGoods goods = getById(id);
            if (goods != null && goods.getDelFlag() == 0) {
                goods.setDelFlag(1);
                goods.setUpdateTime(LocalDateTime.now());
                updateById(goods);
            }
        }
    }

    @Override
    public void updateShelfStatus(Long id, Integer shelfStatus) {
        if (shelfStatus == null || (shelfStatus != 0 && shelfStatus != 1)) {
            throw new RuntimeException("上架状态值无效（0=下架，1=上架）");
        }
        AuctionGoods goods = getById(id);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        goods.setShelfStatus(shelfStatus);
        goods.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(goods);
        if (!success) {
            throw new RuntimeException("更新上架状态失败");
        }
    }

    @Override
    public void auditGoods(Long id, Integer auditStatus, String auditRemark) {
        AuctionGoods goods = getById(id);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        goods.setAuditStatus(auditStatus);
        // 审核通过后自动上架；非通过状态默认下架，避免未通过商品出现在前台
        goods.setShelfStatus(auditStatus != null && auditStatus == 1 ? 1 : 0);
        if (auditRemark != null) {
            goods.setAuditRemark(auditRemark);
        }
        goods.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(goods);
        if (!success) {
            throw new RuntimeException("审核失败");
        }
    }

    /**
     * 为商品加载文件信息
     */
    private void loadFilesForGoods(AuctionGoods goods) {
        if (goods == null) {
            return;
        }
        // 从fileIds字段加载文件列表
        String fileIds = goods.getFileIds();
        if (fileIds != null && !fileIds.trim().isEmpty()) {
            List<AuctionFile> files = fileService.getFilesByIds(fileIds);
            goods.setFiles(files);
        } else {
            goods.setFiles(new ArrayList<>());
        }
    }

    /**
     * 将Map转换为AuctionGoods对象
     */
    private AuctionGoods convertMapToGoods(Map<String, Object> requestData) {
        AuctionGoods goods = new AuctionGoods();
        
        if (requestData.get("goodsName") != null) {
            goods.setGoodsName(requestData.get("goodsName").toString());
        }
        if (requestData.get("categoryId") != null) {
            goods.setCategoryId(requestData.get("categoryId").toString());
        }
        if (requestData.get("goodsDesc") != null) {
            goods.setGoodsDesc(requestData.get("goodsDesc").toString());
        }
        if (requestData.get("basePrice") != null) {
            goods.setBasePrice(new java.math.BigDecimal(requestData.get("basePrice").toString()));
        }
        if (requestData.get("addPrice") != null) {
            goods.setAddPrice(new java.math.BigDecimal(requestData.get("addPrice").toString()));
        }
        if (requestData.get("reservePrice") != null) {
            goods.setReservePrice(new java.math.BigDecimal(requestData.get("reservePrice").toString()));
        }
        if (requestData.get("startTime") != null) {
            goods.setStartTime(parseDateTime(requestData.get("startTime").toString()));
        }
        if (requestData.get("endTime") != null) {
            goods.setEndTime(parseDateTime(requestData.get("endTime").toString()));
        }
        if (requestData.get("auditStatus") != null) {
            goods.setAuditStatus(Integer.parseInt(requestData.get("auditStatus").toString()));
        }
        if (requestData.get("goodsStatus") != null) {
            goods.setGoodsStatus(Integer.parseInt(requestData.get("goodsStatus").toString()));
        }
        if (requestData.get("sellerId") != null) {
            goods.setSellerId(Long.parseLong(requestData.get("sellerId").toString()));
        }
        if (requestData.get("fileIds") != null) {
            goods.setFileIds(requestData.get("fileIds").toString());
        }
        if (requestData.get("shelfStatus") != null) {
            goods.setShelfStatus(Integer.parseInt(requestData.get("shelfStatus").toString()));
        }
        
        return goods;
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 处理ISO格式（包含T和Z）
            if (dateTimeStr.contains("T")) {
                String cleaned = dateTimeStr.replace("Z", "").replace("+00:00", "");
                if (cleaned.contains(".")) {
                    cleaned = cleaned.substring(0, cleaned.indexOf("."));
                }
                return LocalDateTime.parse(cleaned, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } else {
                // 处理标准格式
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception e) {
            throw new RuntimeException("日期时间格式错误: " + dateTimeStr);
        }
    }

    /**
     * 处理文件ID列表（新增时）
     */
    private void processFileIds(Long goodsId, Map<String, Object> requestData) {
        List<Long> fileIds = extractFileIds(requestData);
        
        // 将文件ID列表转换为逗号分隔的字符串，保存到goods表的file_ids字段
        if (!fileIds.isEmpty()) {
            StringBuilder fileIdsStr = new StringBuilder();
            for (int i = 0; i < fileIds.size(); i++) {
                if (i > 0) {
                    fileIdsStr.append(",");
                }
                fileIdsStr.append(fileIds.get(i));
            }
            
            // 更新商品的file_ids字段
            AuctionGoods goods = getById(goodsId);
            if (goods != null) {
                goods.setFileIds(fileIdsStr.toString());
                updateById(goods);
            }
        }
    }

    /**
     * 处理文件ID列表（更新时）
     */
    private void processFileIdsForUpdate(Long goodsId, Map<String, Object> requestData) {
        List<Long> newFileIds = extractFileIds(requestData);
        
        // 获取商品原有的file_ids
        AuctionGoods goods = getById(goodsId);
        List<Long> oldFileIds = new ArrayList<>();
        if (goods != null && goods.getFileIds() != null && !goods.getFileIds().trim().isEmpty()) {
            String[] ids = goods.getFileIds().split(",");
            for (String id : ids) {
                try {
                    oldFileIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效的ID
                }
            }
        }
        
        // 将新文件ID列表转换为逗号分隔的字符串，保存到goods表的file_ids字段
        if (!newFileIds.isEmpty()) {
            StringBuilder fileIdsStr = new StringBuilder();
            for (int i = 0; i < newFileIds.size(); i++) {
                if (i > 0) {
                    fileIdsStr.append(",");
                }
                fileIdsStr.append(newFileIds.get(i));
            }
            
            // 更新商品的file_ids字段
            if (goods != null) {
                goods.setFileIds(fileIdsStr.toString());
                updateById(goods);
            }
        } else {
            // 如果没有新文件，清空file_ids
            if (goods != null) {
                goods.setFileIds(null);
                updateById(goods);
            }
        }
    }

    /**
     * 从requestData中提取文件ID列表
     */
    private List<Long> extractFileIds(Map<String, Object> requestData) {
        List<Long> fileIds = new ArrayList<>();
        Object fileIdsObj = requestData.get("fileIds");
        if (fileIdsObj != null) {
            if (fileIdsObj instanceof List) {
                // 如果是数组
                List<?> list = (List<?>) fileIdsObj;
                for (Object item : list) {
                    if (item != null) {
                        try {
                            fileIds.add(Long.parseLong(item.toString()));
                        } catch (NumberFormatException e) {
                            // 忽略无效的ID
                        }
                    }
                }
            } else if (fileIdsObj instanceof String) {
                // 如果是逗号分隔的字符串
                String[] ids = fileIdsObj.toString().split(",");
                for (String id : ids) {
                    try {
                        fileIds.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException e) {
                        // 忽略无效的ID
                    }
                }
            }
        }
        return fileIds;
    }

    @Override
    public PageInfo<AuctionGoods> getGoodsListForApi(Integer current, Integer size, String keyword, Integer status) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.eq("audit_status", 1); // 只显示审核通过的商品
        wrapper.eq("shelf_status", 1); // 只显示已上架的商品
        
        // 只显示未开始和进行中的商品（0=未开始 1=竞拍中）
        wrapper.in("goods_status", 0, 1);
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("goods_name", keyword);
        }
        
        // 状态筛选（如果指定了状态，则进一步筛选）
        if (status != null) {
            wrapper.eq("goods_status", status);
        }
        
        wrapper.orderByDesc("create_time");

        List<AuctionGoods> list = list(wrapper);
        
        // 自动更新每个商品的状态
        for (AuctionGoods goods : list) {
            updateGoodsStatusByTime(goods);
            loadFilesForGoods(goods);
        }
        
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AuctionGoods> searchGoods(Integer current, Integer size, String keyword) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.eq("audit_status", 1); // 只显示审核通过的商品
        wrapper.eq("shelf_status", 1); // 只显示已上架的商品
        
        // 只显示未开始和进行中的商品（0=未开始 1=竞拍中）
        wrapper.in("goods_status", 0, 1);
        
        // 关键词搜索（商品名称或描述）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("goods_name", keyword).or().like("goods_desc", keyword));
        }
        
        wrapper.orderByDesc("create_time");

        List<AuctionGoods> list = list(wrapper);
        
        // 自动更新每个商品的状态
        for (AuctionGoods goods : list) {
            updateGoodsStatusByTime(goods);
            loadFilesForGoods(goods);
        }
        
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AuctionGoods> getMyGoodsList(Integer current, Integer size, Long userId) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.eq("seller_id", userId);
        
        wrapper.orderByDesc("create_time");

        List<AuctionGoods> list = list(wrapper);
        
        // 自动更新每个商品的状态
        for (AuctionGoods goods : list) {
            updateGoodsStatusByTime(goods);
            loadFilesForGoods(goods);
        }
        
        return new PageInfo<>(list);
    }

    @Override
    public void updateGoodsStatusByTime(AuctionGoods goods) {
        if (goods == null || goods.getStartTime() == null || goods.getEndTime() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Integer currentStatus = goods.getGoodsStatus();
        Integer newStatus;

        // 根据时间计算应该的状态
        if (now.isBefore(goods.getStartTime())) {
            // 当前时间在开始时间之前 -> 未开始
            newStatus = 0;
        } else if (now.isAfter(goods.getEndTime())) {
            // 当前时间在结束时间之后 -> 结算竞拍结果（中标生成订单/流拍）
            newStatus = settleAuctionResult(goods, now);
        } else {
            // 当前时间在开始时间和结束时间之间 -> 竞拍中
            newStatus = 1;
        }

        // 如果状态发生变化，更新数据库
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            goods.setGoodsStatus(newStatus);
            goods.setUpdateTime(LocalDateTime.now());
            updateById(goods);
        }
    }

    /**
     * 竞拍结束后结算结果：
     * - 有最高出价且未生成订单：生成中标订单，状态=已成交(2)
     * - 无出价记录：状态=已流拍(3)
     */
    private Integer settleAuctionResult(AuctionGoods goods, LocalDateTime now) {
        if (orderService.existsActiveOrderByGoodsId(goods.getId())) {
            return 2;
        }

        AuctionRecord highestRecord = recordMapper.selectHighestRecord(goods.getId());
        if (highestRecord == null) {
            return 3;
        }

        orderService.createWinningOrder(
                goods.getId(),
                goods.getSellerId(),
                highestRecord,
                now.plusHours(24)
        );
        return 2;
    }

    @Override
    public void autoOfflineExpiredGoods() {
        LocalDateTime now = LocalDateTime.now();
        
        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.eq("audit_status", 1); // 只处理审核通过的商品
        wrapper.le("end_time", now); // 结束时间小于等于当前时间
        wrapper.in("goods_status", 0, 1); // 只处理未开始或竞拍中的商品
        
        List<AuctionGoods> expiredGoods = list(wrapper);
        
        for (AuctionGoods goods : expiredGoods) {
            // 先更新商品状态
            updateGoodsStatusByTime(goods);
            
            // 然后自动下架（设置auditStatus=3）
            goods.setAuditStatus(3); // 已下架
            goods.setUpdateTime(LocalDateTime.now());
            updateById(goods);
        }
    }

    @Override
    public void reapplyGoods(Long id, Long userId) {
        AuctionGoods goods = getById(id);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        
        // 验证是否为商品所有者
        if (!goods.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作此商品");
        }
        
        // 支持以下场景重新申请：
        // 1) 审核驳回(auditStatus=2)
        // 2) 已下架(auditStatus=3)
        // 3) 已流拍(goodsStatus=3)
        boolean canReapply = (goods.getAuditStatus() != null && (goods.getAuditStatus() == 2 || goods.getAuditStatus() == 3))
                || (goods.getGoodsStatus() != null && goods.getGoodsStatus() == 3);
        if (!canReapply) {
            throw new RuntimeException("仅审核驳回、已下架或已流拍商品可重新申请");
        }
        
        // 重新设置为待审核状态
        goods.setAuditStatus(0); // 待审核
        goods.setShelfStatus(0); // 待审核期间保持下架
        goods.setAuditRemark(null); // 清空之前的审核备注
        goods.setUpdateTime(LocalDateTime.now());
        
        // 根据时间重新计算商品状态
        updateGoodsStatusByTime(goods);
        
        boolean success = updateById(goods);
        if (!success) {
            throw new RuntimeException("重新申请上架失败");
        }
    }

    @Override
    public List<AuctionGoods> getGuessYouLikeGoods(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0)
                .eq("shelf_status", 1)
                .eq("audit_status", 1)
                .in("goods_status", 0, 1)
                .orderByDesc("view_count")
                .last("LIMIT " + safeLimit);
        List<AuctionGoods> list = list(wrapper);
        // 若无符合条件的商品（未开始/竞拍中），放宽为仅需上架且审核通过（包含已结束的）
        if (list.isEmpty()) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0)
                    .eq("shelf_status", 1)
                    .eq("audit_status", 1)
                    .orderByDesc("view_count")
                    .last("LIMIT " + safeLimit);
            list = list(wrapper);
        }
        for (AuctionGoods goods : list) {
            updateGoodsStatusByTime(goods);
            loadFilesForGoods(goods);
        }
        return list;
    }

    @Override
    public void incrementViewCount(Long goodsId) {
        if (goodsId == null) return;
        baseMapper.incrementViewCount(goodsId);
    }
}
