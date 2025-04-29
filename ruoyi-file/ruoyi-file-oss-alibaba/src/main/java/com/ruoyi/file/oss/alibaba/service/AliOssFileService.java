package com.ruoyi.file.oss.alibaba.service;

import java.io.InputStream;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.oss.alibaba.config.AliOssConfig;
import com.ruoyi.file.oss.alibaba.domain.AliOssBucket;
import com.ruoyi.file.service.FileService;

/**
 * oss文件操作实现类
 */
@Component("file:strategy:oss")
@ConditionalOnProperty(prefix = "oss", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class AliOssFileService implements FileService {

    @Autowired
    private AliOssConfig aliOssConfig;

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        aliOssBucket.put(filePath, file);
        return aliOssBucket.generatePublicURL(filePath).toString();
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        return aliOssBucket.get(filePath).getFileInputSteam();
    }

    @Override
    public FileEntity getFile(String filePath) throws Exception {
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
    public URL generatePresignedUrl(String filePath) {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        return aliOssBucket.generatePresignedUrl(filePath, 3600);
    }

    @Override
    public String generatePublicURL(String filePath) throws Exception {
        AliOssBucket aliOssBucket = aliOssConfig.getPrimaryBucket();
        return aliOssBucket.generatePublicURL(filePath).toString();
    }
}