package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionGoodsMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
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

        // 为每个商品加载文件信息
        for (AuctionGoods goods : list) {
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
        // 加载文件信息
        loadFilesForGoods(goods);
        return goods;
    }

    @Override
    public AuctionGoods addGoods(Map<String, Object> requestData) {
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
        goods.setSellerId(1L); // TODO: 从当前登录用户获取
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
    public void auditGoods(Long id, Integer auditStatus, String auditRemark) {
        AuctionGoods goods = getById(id);
        if (goods == null || goods.getDelFlag() == 1) {
            throw new RuntimeException("商品不存在");
        }
        goods.setAuditStatus(auditStatus);
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
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("goods_name", keyword);
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq("goods_status", status);
        }
        
        wrapper.orderByDesc("create_time");

        List<AuctionGoods> list = list(wrapper);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<AuctionGoods> searchGoods(Integer current, Integer size, String keyword) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.eq("audit_status", 1); // 只显示审核通过的商品
        
        // 关键词搜索（商品名称或描述）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("goods_name", keyword).or().like("goods_desc", keyword));
        }
        
        wrapper.orderByDesc("create_time");

        List<AuctionGoods> list = list(wrapper);
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
        return new PageInfo<>(list);
    }
}
