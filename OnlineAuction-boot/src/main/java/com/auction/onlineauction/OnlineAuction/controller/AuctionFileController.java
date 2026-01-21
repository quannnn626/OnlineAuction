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
 * 商品文件表 前端控制器
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
     */
    @PostMapping("/upload")
    public Result<List<AuctionFile>> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                                @RequestParam(value = "goodsId", required = false) Long goodsId) {
        try {
            List<AuctionFile> uploadedFiles = fileService.uploadFiles(files, goodsId);
            return Result.success("上传成功", uploadedFiles);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 根据商品ID获取文件列表
     */
    @GetMapping("/list/{goodsId}")
    public Result<List<AuctionFile>> getFilesByGoodsId(@PathVariable Long goodsId) {
        try {
            List<AuctionFile> files = fileService.getFilesByGoodsId(goodsId);
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