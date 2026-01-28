package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 通用文件表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2026-01-07
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionFile")
public class AuctionFileController {

    @Autowired
    private IAuctionFileService fileService;

    /**
     * 上传文件（图片或视频）
     * @param files 文件数组
     * @param fileCategory 文件分类（goods=商品文件，avatar=用户头像等），默认为goods
     */
    @PostMapping("/upload")
    public Result<List<AuctionFile>> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                                @RequestParam(value = "fileCategory", required = false, defaultValue = "goods") String fileCategory) {
        try {
            List<AuctionFile> uploadedFiles = fileService.uploadFiles(files, fileCategory);
            return Result.success("上传成功", uploadedFiles);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 根据文件ID列表获取文件列表（支持逗号分隔的ID字符串）
     * @param fileIds 文件ID列表（逗号分隔的字符串，如："1,2,3"）
     */
    @GetMapping("/list")
    public Result<List<AuctionFile>> getFilesByIds(@RequestParam(value = "fileIds", required = false) String fileIds) {
        try {
            List<AuctionFile> files = fileService.getFilesByIds(fileIds);
            return Result.success("查询成功", files);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}