package com.ruoyi.file.oss.alibaba.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.oss.alibaba.config.AliOssManagement;
import com.ruoyi.file.oss.alibaba.domain.AliOssBucket;
import com.ruoyi.file.storage.StorageEntity;
import com.ruoyi.file.storage.StorageService;

/**
 * oss文件操作实现类
 */
@Component("file:strategy:oss")
@ConditionalOnProperty(prefix = "oss", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class AliOssFileService implements StorageService {

    @Autowired
    private AliOssManagement aliOssConfig;

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        aliOssBucket.put(filePath, file);
        return generateUrl(filePath);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        return aliOssBucket.get(filePath).getInputStream();
    }

    @Override
    public StorageEntity getFile(String filePath) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        return aliOssBucket.get(filePath);
    };

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        aliOssBucket.remove(filePath);
        return true;
    }

    @Override
    public String generateUrl(String filePath) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        if (aliOssBucket.getPermission() == "public") {
            return aliOssBucket.generatePublicURL(filePath).toString();
        } else {
            return aliOssBucket.generatePresignedUrl(filePath, 3600).toString();
        }
    }

    @Override
    public String uploadFileByMultipart(MultipartFile file, String filePath, double partSize) throws Exception {
        return aliOssConfig.getPrimaryBucket().uploadFileByMultipart(file, filePath, partSize);
    }
}