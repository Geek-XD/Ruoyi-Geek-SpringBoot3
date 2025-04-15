package com.ruoyi.file.oss.alibaba.service;

import java.io.InputStream;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.oss.alibaba.config.AliOssConfig;
import com.ruoyi.file.oss.alibaba.domain.AliOssFileVO;
import com.ruoyi.file.oss.alibaba.exception.AliOssClientErrorException;
import com.ruoyi.file.oss.alibaba.exception.AliOssClientNotFundException;
import com.ruoyi.file.oss.alibaba.utils.AliOssUtil;
import com.ruoyi.file.service.FileService;
import com.ruoyi.file.utils.FileOperateUtils;

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
        return upload(aliOssConfig.getPrimary(), filePath, file);
    }

    @Override
    public String upload(String baseDir, String filePath, MultipartFile file) throws Exception {
        String relativePath = null;
        if (FileUtils.isAbsolutePath(filePath)) {
            relativePath = FileUtils.getRelativePath(filePath);
        } else {
            relativePath = filePath;
        }
        return AliOssUtil.uploadFile(baseDir, relativePath, file);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        AliOssFileVO file = AliOssUtil.getFile(filePath);
        return file.getFileInputSteam();
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        AliOssUtil.removeFile(aliOssConfig.getPrimary(), filePath);
        FileOperateUtils.deleteFileAndMd5ByFilePath(filePath);
        return true;
    }

    @Override
    public FileEntity getFile(String filePath) throws Exception {
        return AliOssUtil.getFile(filePath);
    };

    /**
     * 生成预签名Oss URL.
     *
     * @param filePath 文件路径
     * @return 预签名URL
     * @throws AliOssClientNotFundException 如果找不到对应的配置时抛出
     * @throws AliOssClientErrorException   如果在创建或获取预签名URL过程中发生错误时抛出
     */
    @Override
    public URL generatePresignedUrl(String filePath) throws Exception {
        return AliOssUtil.generatePresignedUrl(filePath);
    }
}