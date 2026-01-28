package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 通用文件表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2026-01-07
 */
public interface IAuctionFileService extends IService<AuctionFile> {

    /**
     * 上传文件（图片或视频）
     * @param files 文件数组
     * @param fileCategory 文件分类（goods=商品文件，avatar=用户头像等）
     * @return 上传成功的文件列表
     */
    List<AuctionFile> uploadFiles(MultipartFile[] files, String fileCategory);

    /**
     * 根据文件ID列表获取文件列表（支持逗号分隔的ID字符串）
     * @param fileIds 文件ID列表（逗号分隔的字符串，如："1,2,3"）
     * @return 文件列表
     */
    List<AuctionFile> getFilesByIds(String fileIds);

    /**
     * 根据文件ID列表获取文件列表
     * @param fileIds 文件ID列表
     * @return 文件列表
     */
    List<AuctionFile> getFilesByIds(List<Long> fileIds);

    /**
     * 删除文件（逻辑删除并删除物理文件）
     */
    void deleteFile(Long id);
}