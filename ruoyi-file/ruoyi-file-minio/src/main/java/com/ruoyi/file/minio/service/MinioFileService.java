package com.ruoyi.file.minio.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.minio.config.MinioManagement;
import com.ruoyi.file.minio.domain.MinioBucket;
import com.ruoyi.file.storage.StorageEntity;
import com.ruoyi.file.storage.StorageService;

/**
 * Minio文件操作实现类
 */
@Component("file:strategy:minio")
@ConditionalOnProperty(prefix = "minio", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class MinioFileService implements StorageService {

    @Autowired
    private MinioManagement minioConfig;

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        minioBucket.put(filePath, file);
        return generateUrl(filePath);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        return minioBucket.get(filePath).getInputStream();
    }

    @Override
    public StorageEntity getFile(String filePath) throws Exception {
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
        if ("public".equals(minioBucket.getPermission())) {
            return minioBucket.generatePublicURL(filePath).toString();
        } else {
            return minioBucket.generatePresignedUrl(filePath, 3600).toString();
        }
    }

    @Override
    public String initMultipartUpload(String filePath) throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        return minioBucket.initMultipartUpload(filePath);
    }

    @Override
    public String uploadPart(String filePath, String uploadId, int partNumber, long partSize, InputStream inputStream)
            throws Exception {
        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        return minioBucket.uploadPart(filePath, uploadId, partNumber, partSize, inputStream).getETag();
    }

    @Override
    public String completeMultipartUpload(String filePath, String uploadId, List<Map<String, Object>> partETags)
            throws Exception {
        if (partETags == null || partETags.isEmpty()) {
            throw new IllegalArgumentException("分片标识列表不能为空");
        }

        MinioBucket minioBucket = minioConfig.getPrimaryBucket();
        return minioBucket.completeMultipartUpload(filePath, uploadId, partETags);
    }

}