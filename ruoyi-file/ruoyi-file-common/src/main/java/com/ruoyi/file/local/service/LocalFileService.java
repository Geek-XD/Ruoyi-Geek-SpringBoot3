package com.ruoyi.file.local.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.local.config.LocalManagement;
import com.ruoyi.file.local.domain.LocalBucket;
import com.ruoyi.file.storage.StorageEntity;
import com.ruoyi.file.storage.StorageService;

/**
 * 磁盘文件操作实现类
 */
@Component("file:strategy:local")
@ConditionalOnProperty(prefix = "local", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class LocalFileService implements StorageService {

    @Autowired
    LocalManagement localConfig;

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        primaryBucket.put(filePath, file);
        return generateUrl(filePath);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.get(filePath).getInputStream();
    }

    @Override
    public StorageEntity getFile(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.get(filePath);
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        primaryBucket.remove(filePath);
        return true;
    }

    @Override
    public String generateUrl(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        if ("public".equals(primaryBucket.getPermission())) {
            return primaryBucket.generatePublicURL(filePath).toString();
        } else {
            return primaryBucket.generatePresignedUrl(filePath, 3600).toString();
        }
    }

    @Override
    public String initMultipartUpload(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.initMultipartUpload(filePath);
    }

    @Override
    public String uploadPart(String filePath, String uploadId, int partNumber, long partSize, InputStream inputStream)
            throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.uploadPart(filePath, uploadId, partNumber, partSize, inputStream);
    }

    @Override
    public String completeMultipartUpload(String filePath, String uploadId, List<Map<String, Object>> partETags)
            throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.completeMultipartUpload(filePath, uploadId, partETags);
    }
}
