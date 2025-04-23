package com.ruoyi.file.minio.service;

import java.io.InputStream;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.minio.config.MinioConfig;
import com.ruoyi.file.minio.domain.MinioFileVO;
import com.ruoyi.file.minio.exception.MinioClientErrorException;
import com.ruoyi.file.minio.exception.MinioClientNotFundException;
import com.ruoyi.file.minio.utils.MinioUtil;
import com.ruoyi.file.service.FileService;
import com.ruoyi.file.utils.FileOperateUtils;

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
        String relativePath = FileUtils.getRelativePath(filePath);
        MinioUtil.uploadFile(minioConfig.getPrimary(), relativePath, file);
        return generatePublicURL(relativePath);
    }

    @Override
    public String generatePublicURL(String filePath) {
        return MinioUtil.getURL(minioConfig.getPrimary(), filePath);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        MinioFileVO file = MinioUtil.getFile(minioConfig.getPrimary(), filePath);
        return file.getFileInputSteam();
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        MinioUtil.removeFile(minioConfig.getPrimary(), filePath);
        FileOperateUtils.deleteFileAndMd5ByFilePath(filePath);
        return true;
    }

    @Override
    public FileEntity getFile(String filePath) throws Exception {
        return MinioUtil.getFile(filePath);
    }

    /**
     * 生成预签名Minio URL.
     *
     * @param filePath 文件路径
     * @return 预签名URL
     * @throws MinioClientNotFundException 如果找不到对应的配置时抛出
     * @throws MinioClientErrorException   如果在创建或获取预签名URL过程中发生错误时抛出
     */
    @Override
    public URL generatePresignedUrl(String filePath) throws Exception {
        return MinioUtil.generatePresignedUrl(filePath);
    }
}