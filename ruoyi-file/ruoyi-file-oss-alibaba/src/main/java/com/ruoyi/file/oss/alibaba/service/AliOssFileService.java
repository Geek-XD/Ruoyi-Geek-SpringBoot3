package com.ruoyi.file.oss.alibaba.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.model.PartETag;
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
        if ("public".equals(aliOssBucket.getPermission())) {
            return aliOssBucket.generatePublicURL(filePath).toString();
        } else {
            return aliOssBucket.generatePresignedUrl(filePath, 3600).toString();
        }
    }

    @Override
    public String initMultipartUpload(String filePath) throws Exception {
        return aliOssConfig.getPrimaryBucket().initMultipartUpload(filePath);
    }

    @Override
    public String uploadPart(String filePath, String uploadId, int partNumber, long partSize, InputStream inputStream)
            throws Exception {
        AliOssBucket bucket = aliOssConfig.getPrimaryBucket();
        return bucket.uploadPart(filePath, uploadId, partNumber, partSize, inputStream).getETag();
    }

    @Override
    public String completeMultipartUpload(String filePath, String uploadId, List<Map<String, Object>> partETags)
            throws Exception {
        if (partETags == null || partETags.isEmpty()) {
            throw new IllegalArgumentException("分片ETag列表不能为空");
        }

        // 将Map转换为PartETag对象
        List<PartETag> ossPartETags = new ArrayList<>();
        for (Map<String, Object> part : partETags) {
            int partNumber = ((Number) part.get("partNumber")).intValue();
            String etag = (String) part.get("etag");
            ossPartETags.add(new PartETag(partNumber, etag));
        }
        return aliOssConfig.getPrimaryBucket().completeMultipartUpload(filePath, uploadId, ossPartETags);
    }

}