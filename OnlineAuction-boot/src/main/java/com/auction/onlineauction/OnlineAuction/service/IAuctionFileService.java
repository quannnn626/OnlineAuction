package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 商品文件表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2026-01-07
 */
public interface IAuctionFileService extends IService<AuctionFile> {

    /**
     * 上传文件（图片或视频）
     */
    List<AuctionFile> uploadFiles(MultipartFile[] files, Long goodsId);

    /**
     * 根据商品ID获取文件列表
     */
    List<AuctionFile> getFilesByGoodsId(Long goodsId);

    /**
     * 删除文件（逻辑删除并删除物理文件）
     */
    void deleteFile(Long id);
}