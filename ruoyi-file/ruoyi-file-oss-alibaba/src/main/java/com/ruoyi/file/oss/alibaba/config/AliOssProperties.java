package com.ruoyi.file.oss.alibaba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.aliyun.oss.OSSClient;

@ConfigurationProperties(prefix = "oss.client")
public class AliOssProperties {
 
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    private OSSClient ossClient;

    public AliOssProperties(){ }
    
    public AliOssProperties(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
    }
    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public String getAccessKeyId() {
        return accessKeyId;
    }
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    public String getAccessKeySecret() {
        return accessKeySecret;
    }
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
    public String getBucketName() {
        return bucketName;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public OSSClient getOssClient() {
        return ossClient;
    }

    public void setOssClient(OSSClient ossClient) {
        this.ossClient = ossClient;
    }

    
 
}