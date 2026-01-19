package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 拍卖商品表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionGoods")
public class AuctionGoodsController {

    @Autowired
    private IAuctionGoodsService goodsService;

    @Autowired
    private IAuctionFileService fileService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionGoods>> getGoodsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String goodsName,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer goodsStatus) {
        try {
            // 使用 PageHelper 开始分页
            PageHelper.startPage(current, size);

            QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            if (goodsName != null && !goodsName.trim().isEmpty()) {
                wrapper.like("goods_name", goodsName);
            }
            if (categoryId != null) {
                wrapper.eq("category_id", categoryId);
            }
            if (auditStatus != null) {
                wrapper.eq("audit_status", auditStatus);
            }
            if (goodsStatus != null) {
                wrapper.eq("goods_status", goodsStatus);
            }
            wrapper.orderByDesc("create_time");

            // 查询数据（此时会自动分页）
            List<AuctionGoods> list = goodsService.list(wrapper);

            // 为每个商品加载文件信息
            for (AuctionGoods goods : list) {
                List<AuctionFile> files = fileService.lambdaQuery()
                        .eq(AuctionFile::getGoodsId, goods.getId())
                        .eq(AuctionFile::getDelFlag, 0)
                        .list();
                goods.setFiles(files);
            }

            // 使用 PageInfo 包装结果
            PageInfo<AuctionGoods> pageInfo = new PageInfo<>(list);

            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有商品列表（不分页，用于下拉选择等）
     */
    @GetMapping("/list")
    public Result<List<AuctionGoods>> getGoodsList() {
        try {
            QueryWrapper<AuctionGoods> wrapper = new QueryWrapper<>();
            wrapper.eq("del_flag", 0);
            wrapper.orderByDesc("create_time");
            List<AuctionGoods> list = goodsService.list(wrapper);

            // 为每个商品加载文件信息
            for (AuctionGoods goods : list) {
                List<AuctionFile> files = fileService.lambdaQuery()
                        .eq(AuctionFile::getGoodsId, goods.getId())
                        .eq(AuctionFile::getDelFlag, 0)
                        .list();
                goods.setFiles(files);
            }

            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public Result<AuctionGoods> getGoodsById(@PathVariable Long id) {
        try {
            AuctionGoods goods = goodsService.getById(id);
            if (goods == null || goods.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            // 加载文件信息
            List<AuctionFile> files = fileService.lambdaQuery()
                    .eq(AuctionFile::getGoodsId, id)
                    .eq(AuctionFile::getDelFlag, 0)
                    .list();
            goods.setFiles(files);
            return Result.success("查询成功", goods);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增商品
     */
    @PostMapping("/addGoods")
    public Result<AuctionGoods> addGoods(@RequestBody Map<String, Object> requestData) {
        try {
            // 将Map转换为AuctionGoods对象
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
                String startTimeStr = requestData.get("startTime").toString();
                try {
                    // 处理前端传递的日期时间格式（可能是ISO格式或时间戳）
                    if (startTimeStr.contains("T")) {
                        goods.setStartTime(LocalDateTime.parse(startTimeStr.replace("Z", "")));
                    } else {
                        goods.setStartTime(LocalDateTime.parse(startTimeStr));
                    }
                } catch (Exception e) {
                    // 如果解析失败，忽略该字段
                }
            }
            if (requestData.get("endTime") != null) {
                String endTimeStr = requestData.get("endTime").toString();
                try {
                    // 处理前端传递的日期时间格式（可能是ISO格式或时间戳）
                    if (endTimeStr.contains("T")) {
                        goods.setEndTime(LocalDateTime.parse(endTimeStr.replace("Z", "")));
                    } else {
                        goods.setEndTime(LocalDateTime.parse(endTimeStr));
                    }
                } catch (Exception e) {
                    // 如果解析失败，忽略该字段
                }
            }
            
            // 验证商品名称
            if (goods.getGoodsName() == null || goods.getGoodsName().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            // 设置默认值
            if (goods.getAuditStatus() == null) {
                goods.setAuditStatus(0); // 待审核
            }
            if (goods.getGoodsStatus() == null) {
                goods.setGoodsStatus(0); // 未开始
            }
            goods.setSellerId(1L);
            goods.setCreateTime(LocalDateTime.now());
            goods.setUpdateTime(LocalDateTime.now());
            goods.setDelFlag(0);
            
            boolean success = goodsService.save(goods);
            if (success) {
                // 获取文件ID列表并更新文件的goodsId
                Object fileIdsObj = requestData.get("fileIds");
                if (fileIdsObj != null) {
                    List<Long> fileIds = new ArrayList<>();
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
                    
                    // 根据文件ID更新goodsId
                    for (Long fileId : fileIds) {
                        AuctionFile file = fileService.getById(fileId);
                        if (file != null && file.getDelFlag() == 0) {
                            file.setGoodsId(goods.getId());
                            fileService.updateById(file);
                        }
                    }
                }
                
                // 兼容处理：如果有files数组
                if (goods.getFiles() != null && !goods.getFiles().isEmpty()) {
                    for (AuctionFile file : goods.getFiles()) {
                        if (file.getId() != null) {
                            file.setGoodsId(goods.getId());
                            fileService.updateById(file);
                        }
                    }
                }
                
                return Result.success("新增成功", goods);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<AuctionGoods> updateGoods(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            // 验证商品是否存在
            AuctionGoods existing = goodsService.getById(id);
            if (existing == null || existing.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            
            // 将Map转换为AuctionGoods对象
            AuctionGoods goods = new AuctionGoods();
            goods.setId(id);
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
                String startTimeStr = requestData.get("startTime").toString();
                try {
                    if (startTimeStr.contains("T")) {
                        goods.setStartTime(LocalDateTime.parse(startTimeStr.replace("Z", "")));
                    } else {
                        goods.setStartTime(LocalDateTime.parse(startTimeStr));
                    }
                } catch (Exception e) {
                    // 如果解析失败，使用原有值
                    goods.setStartTime(existing.getStartTime());
                }
            } else {
                goods.setStartTime(existing.getStartTime());
            }
            if (requestData.get("endTime") != null) {
                String endTimeStr = requestData.get("endTime").toString();
                try {
                    if (endTimeStr.contains("T")) {
                        goods.setEndTime(LocalDateTime.parse(endTimeStr.replace("Z", "")));
                    } else {
                        goods.setEndTime(LocalDateTime.parse(endTimeStr));
                    }
                } catch (Exception e) {
                    // 如果解析失败，使用原有值
                    goods.setEndTime(existing.getEndTime());
                }
            } else {
                goods.setEndTime(existing.getEndTime());
            }
            
            // 验证商品名称
            if (goods.getGoodsName() == null || goods.getGoodsName().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            
            // 设置更新信息
            goods.setUpdateTime(LocalDateTime.now());
            // 保留创建时间、删除标志、审核状态、商品状态、卖方ID等
            goods.setCreateTime(existing.getCreateTime());
            goods.setDelFlag(existing.getDelFlag());
            goods.setAuditStatus(existing.getAuditStatus());
            goods.setGoodsStatus(existing.getGoodsStatus());
            goods.setSellerId(existing.getSellerId());
            
            boolean success = goodsService.updateById(goods);
            if (success) {
                // 处理文件关联更新
                Object fileIdsObj = requestData.get("fileIds");
                if (fileIdsObj != null) {
                    List<Long> newFileIds = new ArrayList<>();
                    if (fileIdsObj instanceof List) {
                        // 如果是数组
                        List<?> list = (List<?>) fileIdsObj;
                        for (Object item : list) {
                            if (item != null) {
                                try {
                                    newFileIds.add(Long.parseLong(item.toString()));
                                } catch (NumberFormatException e) {
                                    // 忽略无效的ID
                                }
                            }
                        }
                    } else if (fileIdsObj instanceof String) {
                        // 如果是逗号分隔的字符串
                        String[] ids = fileIdsObj.toString().split(",");
                        for (String fileId : ids) {
                            try {
                                newFileIds.add(Long.parseLong(fileId.trim()));
                            } catch (NumberFormatException e) {
                                // 忽略无效的ID
                            }
                        }
                    }
                    
                    // 获取商品原有的文件列表
                    List<AuctionFile> oldFiles = fileService.lambdaQuery()
                            .eq(AuctionFile::getGoodsId, id)
                            .eq(AuctionFile::getDelFlag, 0)
                            .list();
                    List<Long> oldFileIds = new ArrayList<>();
                    for (AuctionFile file : oldFiles) {
                        if (file.getId() != null) {
                            oldFileIds.add(file.getId());
                        }
                    }
                    
                    // 找出需要删除的文件（旧文件列表中不在新文件列表中的）
                    List<Long> filesToRemove = new ArrayList<>();
                    for (Long oldFileId : oldFileIds) {
                        if (!newFileIds.contains(oldFileId)) {
                            filesToRemove.add(oldFileId);
                        }
                    }
                    
                    // 删除旧文件关联（逻辑删除）
                    for (Long fileId : filesToRemove) {
                        AuctionFile file = fileService.getById(fileId);
                        if (file != null) {
                            file.setGoodsId(null); // 清除关联
                            fileService.updateById(file);
                        }
                    }
                    
                    // 更新新文件的goodsId
                    for (Long fileId : newFileIds) {
                        AuctionFile file = fileService.getById(fileId);
                        if (file != null && file.getDelFlag() == 0) {
                            file.setGoodsId(id);
                            fileService.updateById(file);
                        }
                    }
                }
                
                // 兼容处理：如果有files数组
                if (requestData.get("files") != null) {
                    Object filesObj = requestData.get("files");
                    if (filesObj instanceof List) {
                        List<?> filesList = (List<?>) filesObj;
                        for (Object fileObj : filesList) {
                            if (fileObj instanceof Map) {
                                Map<?, ?> fileMap = (Map<?, ?>) fileObj;
                                Object fileIdObj = fileMap.get("id");
                                if (fileIdObj != null) {
                                    try {
                                        Long fileId = Long.parseLong(fileIdObj.toString());
                                        AuctionFile file = fileService.getById(fileId);
                                        if (file != null) {
                                            file.setGoodsId(id);
                                            fileService.updateById(file);
                                        }
                                    } catch (NumberFormatException e) {
                                        // 忽略无效的ID
                                    }
                                }
                            }
                        }
                    }
                }
                
                return Result.success("更新成功", goods);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        try {
            AuctionGoods goods = goodsService.getById(id);
            if (goods == null || goods.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            // 逻辑删除
            goods.setDelFlag(1);
            goods.setUpdateTime(LocalDateTime.now());
            boolean success = goodsService.updateById(goods);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除商品
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteGoods(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的商品");
            }
            for (Long id : ids) {
                AuctionGoods goods = goodsService.getById(id);
                if (goods != null && goods.getDelFlag() == 0) {
                    goods.setDelFlag(1);
                    goods.setUpdateTime(LocalDateTime.now());
                    goodsService.updateById(goods);
                }
            }
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 审核商品
     */
    @PutMapping("/audit/{id}")
    public Result<Void> auditGoods(@PathVariable Long id, @RequestParam Integer auditStatus, @RequestParam(required = false) String auditRemark) {
        try {
            AuctionGoods goods = goodsService.getById(id);
            if (goods == null || goods.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            goods.setAuditStatus(auditStatus);
            if (auditRemark != null) {
                goods.setAuditRemark(auditRemark);
            }
            goods.setUpdateTime(LocalDateTime.now());
            boolean success = goodsService.updateById(goods);
            if (success) {
                return Result.success("审核成功", null);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}
