package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFile;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionFileMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 通用文件表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2026-01-07
 */
@Service
public class AuctionFileServiceImpl extends ServiceImpl<AuctionFileMapper, AuctionFile> implements IAuctionFileService {

    @Override
    public List<AuctionFile> uploadFiles(MultipartFile[] files, String fileCategory) {
        List<AuctionFile> uploadedFiles = new ArrayList<>();
        
        // 默认分类为goods（商品文件）
        if (fileCategory == null || fileCategory.trim().isEmpty()) {
            fileCategory = "goods";
        }

        // 过滤空文件
        if (files == null || files.length == 0) {
            return uploadedFiles;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            String fileType = "image";
            if (contentType != null && contentType.startsWith("video/")) {
                fileType = "video";
            } else if (contentType != null && !contentType.startsWith("image/")) {
                throw new RuntimeException("只支持上传图片和视频文件");
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new RuntimeException("文件名格式错误");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 根据文件分类确定上传目录
            String uploadSubDir = fileCategory; // goods, avatar等
            String uploadDir = System.getProperty("user.dir") + "/upload/" + uploadSubDir + "/" + 
                             LocalDateTime.now().getYear() + String.format("%02d", LocalDateTime.now().getMonthValue()) + "/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            String relativePath = "/upload/" + uploadSubDir + "/" + LocalDateTime.now().getYear() +
                            String.format("%02d", LocalDateTime.now().getMonthValue()) + "/" + fileName;
            String fullPath = uploadDir + fileName;
            File destFile = new File(fullPath);
            try {
                file.transferTo(destFile);
            } catch (IOException e) {
                throw new RuntimeException("文件保存失败：" + e.getMessage());
            }

            // 保存到数据库
            AuctionFile auctionFile = new AuctionFile();
            auctionFile.setFileName(originalFilename);
            auctionFile.setFilePath(relativePath);
            auctionFile.setFileType(fileType);
            auctionFile.setCreateTime(LocalDateTime.now());
            auctionFile.setDelFlag(0);

            boolean success = save(auctionFile);
            if (!success) {
                // 如果数据库保存失败，删除已上传的文件
                destFile.delete();
                throw new RuntimeException("文件信息保存失败");
            }
            uploadedFiles.add(auctionFile);
        }

        return uploadedFiles;
    }

    @Override
    public List<AuctionFile> getFilesByIds(String fileIds) {
        if (fileIds == null || fileIds.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> idList = new ArrayList<>();
        String[] ids = fileIds.split(",");
        for (String id : ids) {
            try {
                idList.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                // 忽略无效的ID
            }
        }
        
        return getFilesByIds(idList);
    }

    @Override
    public List<AuctionFile> getFilesByIds(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return lambdaQuery()
                .in(AuctionFile::getId, fileIds)
                .eq(AuctionFile::getDelFlag, 0)
                .list();
    }

    @Override
    public void deleteFile(Long id) {
        AuctionFile file = getById(id);
        if (file == null || file.getDelFlag() == 1) {
            throw new RuntimeException("文件不存在");
        }

        // 删除物理文件
        String filePath = System.getProperty("user.dir") + file.getFilePath();
        File physicalFile = new File(filePath);
        if (physicalFile.exists()) {
            boolean deleted = physicalFile.delete();
            if (!deleted) {
                // 物理文件删除失败，不阻止逻辑删除
            }
        }

        // 逻辑删除数据库记录
        file.setDelFlag(1);
        boolean success = updateById(file);
        if (!success) {
            throw new RuntimeException("删除失败");
        }
    }
}