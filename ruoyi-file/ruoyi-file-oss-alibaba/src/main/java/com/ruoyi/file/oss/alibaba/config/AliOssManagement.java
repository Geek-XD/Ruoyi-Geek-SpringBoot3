package com.ruoyi.file.oss.alibaba.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.ruoyi.file.oss.alibaba.domain.AliOssBucket;
import com.ruoyi.file.storage.StorageManagement;

/**
 * 配置类用于管理阿里云OSS客户端实例及其相关属性。
 */
@Configuration("oss")
@ConditionalOnProperty(prefix = "oss", name = "enable", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "oss")
public class AliOssManagement implements StorageManagement {
    private static final Logger logger = LoggerFactory.getLogger(AliOssManagement.class);
    private Map<String, AliOssBucketProperties> client;
    private String primary;
    private Map<String, AliOssBucket> targetAliOssBucket = new HashMap<>();
    private AliOssBucket primaryBucket;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (client == null || client.isEmpty()) {
            throw new RuntimeException("Client properties cannot be null or empty");
        }

        client.forEach((name, props) -> {
            try {
                AliOssBucket aliOssBucket = createOssClient(name, props);
                targetAliOssBucket.put(name, aliOssBucket);
            } catch (Exception e) {
                logger.error("Failed to create OSS client for {}: {}", name, e.getMessage(), e);
            }
        });

        if (targetAliOssBucket.get(primary) == null) {
            throw new RuntimeException("Primary client " + primary + " does not exist");
        }
        primaryBucket = targetAliOssBucket.get(primary);
    }

    private void validateOssBucket(AliOssBucket aliOssBucket) {
        OSS ossClient = aliOssBucket.getOssClient();
        String bucketName = aliOssBucket.getBucketName();
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                throw new RuntimeException("Bucket " + bucketName + " does not exist");
            }
        } catch (OSSException oe) {
            logger.error("OSSException: " + oe.getMessage(), oe);
            throw new RuntimeException("OSS error: " + oe.getMessage());
        } catch (ClientException ce) {
            logger.error("ClientException: " + ce.getMessage(), ce);
            throw new RuntimeException("Client error: " + ce.getMessage());
        } catch (Exception e) {
            logger.error("Exception: " + e.getMessage(), e);
            throw new RuntimeException("Error validating OSS bucket: " + e.getMessage());
        }
    }

    private AliOssBucket createOssClient(String name, AliOssBucketProperties props) {
        if (props == null || props.getEndpoint() == null || props.getAccessKeyId() == null ||
                props.getAccessKeySecret() == null || props.getBucketName() == null) {
            throw new IllegalArgumentException("AliOssProperties or its required fields cannot be null");
        }

        OSS client = new OSSClientBuilder().build(props.getEndpoint(), props.getAccessKeyId(),
                props.getAccessKeySecret());
        AliOssBucket ossBucket = AliOssBucket.builder()
                .ossClient(client)
                .bucketName(props.getBucketName())
                .endpoint(props.getEndpoint())
                .build();
        validateOssBucket(ossBucket);
        logger.info("数据桶：{} - 链接成功", name);
        return ossBucket;
    }

    public AliOssBucket getPrimaryBucket() {
        return this.primaryBucket;
    }

    @Override
    public AliOssBucket getBucket(String client) {
        return targetAliOssBucket.get(client);
    }

    public Map<String, AliOssBucketProperties> getClient() {
        return client;
    }

    public void setClient(Map<String, AliOssBucketProperties> client) {
        this.client = client;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }
}
