package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            List<AuctionFile> uploadedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // 检查文件类型
                String contentType = file.getContentType();
                String fileType = "image";
                if (contentType != null && contentType.startsWith("video/")) {
                    fileType = "video";
                } else if (contentType != null && !contentType.startsWith("image/")) {
                    return Result.error("只支持上传图片和视频文件");
                }

                // 生成文件名
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

                // 创建上传目录
                String uploadDir = System.getProperty("user.dir") + "/upload/goods/" + LocalDateTime.now().getYear() +
                                 String.format("%02d", LocalDateTime.now().getMonthValue()) + "/";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // 保存文件
                String relativePath = "/upload/goods/" + LocalDateTime.now().getYear() +
                                String.format("%02d", LocalDateTime.now().getMonthValue()) + "/" + fileName;
                String fullPath = uploadDir + fileName;
                File destFile = new File(fullPath);
                file.transferTo(destFile);

                // 保存到数据库
                AuctionFile auctionFile = new AuctionFile();
                auctionFile.setFileName(originalFilename);
                auctionFile.setFilePath(relativePath);
                auctionFile.setGoodsId(goodsId);
                auctionFile.setFileType(fileType);
                auctionFile.setCreateTime(LocalDateTime.now());
                auctionFile.setDelFlag(0);

                fileService.save(auctionFile);
                uploadedFiles.add(auctionFile);
            }

            return Result.success("上传成功", uploadedFiles);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
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
            List<AuctionFile> files = fileService.lambdaQuery()
                    .eq(AuctionFile::getGoodsId, goodsId)
                    .eq(AuctionFile::getDelFlag, 0)
                    .list();
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
            AuctionFile file = fileService.getById(id);
            if (file == null || file.getDelFlag() == 1) {
                return Result.error("文件不存在");
            }

            // 删除物理文件
            File physicalFile = new File(file.getFilePath());
            if (physicalFile.exists()) {
                physicalFile.delete();
            }

            // 逻辑删除数据库记录
            file.setDelFlag(1);
            fileService.updateById(file);

            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}