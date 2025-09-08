package com.ruoyi.file.oss.alibaba.config;

import lombok.Data;

@Data
public class AliOssBucketProperties {
    private String permission;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
}