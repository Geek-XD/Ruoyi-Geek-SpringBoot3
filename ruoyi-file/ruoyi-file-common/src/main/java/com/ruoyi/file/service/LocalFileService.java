package com.ruoyi.file.service;

import java.io.InputStream;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.config.LocalConfig;
import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.domain.LocalBucket;

/**
 * 磁盘文件操作实现类
 */
@Component("file:strategy:local")
@ConditionalOnProperty(prefix = "local", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class LocalFileService implements FileService {

    @Autowired
    LocalConfig localConfig;

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        primaryBucket.put(filePath, file);
        return primaryBucket.generatePublicURL(filePath).toString();
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.get(filePath).getFileInputSteam();
    }

    @Override
    public FileEntity getFile(String filePath) throws Exception {
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
    public URL generatePresignedUrl(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.generatePresignedUrl(filePath, 3600);
    }

    @Override
    public String generatePublicURL(String filePath) throws Exception {
        LocalBucket primaryBucket = localConfig.getPrimaryBucket();
        return primaryBucket.generatePublicURL(filePath).toString();
    }
}
