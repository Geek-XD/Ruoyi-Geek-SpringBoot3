package com.ruoyi.file.minio.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.minio.config.MinioConfig;
import com.ruoyi.file.minio.domain.MinioBucket;
import com.ruoyi.file.service.FileService;

/**
 * Minio文件操作实现类
 */
@Component("file:strategy:minio")
@ConditionalOnProperty(prefix = "minio", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class MinioFileService implements FileService {

    @Autowired
    private MinioConfig minioConfig;

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        minioBucket.put(filePath, file);
        return generateUrl(filePath);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        return minioBucket.get(filePath).getFileInputSteam();
    }

    @Override
    public FileEntity getFile(String filePath) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        return minioBucket.get(filePath);
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        minioBucket.remove(filePath);
        return true;
    }

    @Override
    public String generateUrl(String filePath) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        if (minioBucket.getPermission() == "public") {
            return minioBucket.generatePublicURL(filePath).toString();
        } else {
            return minioBucket.generatePresignedUrl(filePath, 3600).toString();
        }
    }

}