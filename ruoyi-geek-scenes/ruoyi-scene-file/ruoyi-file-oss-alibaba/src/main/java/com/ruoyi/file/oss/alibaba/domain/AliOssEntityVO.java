package com.ruoyi.file.oss.alibaba.domain;

import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.ruoyi.file.storage.StorageEntity;

public class AliOssEntityVO extends StorageEntity {
    private String key;
    private String bucketName;
    private ObjectMetadata metadata;

    public AliOssEntityVO(OSSObject object, String filePath) {
        this.setInputStream(object.getObjectContent());
        this.setKey(object.getKey());
        this.setBucketName(object.getBucketName());
        this.setByteCount(object.getObjectMetadata().getContentLength());
        this.setFilePath(filePath);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }
}