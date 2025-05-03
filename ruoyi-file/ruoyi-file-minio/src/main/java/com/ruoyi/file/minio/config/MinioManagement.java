package com.ruoyi.file.minio.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.io.EmptyInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.file.minio.domain.MinioBucket;
import com.ruoyi.file.storage.StorageManagement;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Configuration("minio")
@ConditionalOnProperty(prefix = "minio", name = { "enable" }, havingValue = "true", matchIfMissing = false)
@ConfigurationProperties("minio")
public class MinioManagement implements StorageManagement {
    private static final Logger logger = LoggerFactory.getLogger(MinioManagement.class);
    private Map<String, MinioBucketProperties> client;
    private String primary;
    private Map<String, MinioBucket> targetMinioBucket = new HashMap<>();
    private MinioBucket primaryBucket;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (client == null || client.isEmpty()) {
            throw new RuntimeException("Client properties cannot be null or empty");
        }
        client.forEach((name, props) -> {
            try {
                targetMinioBucket.put(name, createMinioClient(name, props));
            } catch (Exception e) {
                logger.error("Failed to create MinIO client for {}: {}", name, e.getMessage(), e);
            }
        });

        if (targetMinioBucket.get(primary) == null) {
            throw new RuntimeException("Primary client " + primary + " does not exist");
        }
        primaryBucket = targetMinioBucket.get(primary);
    }

    private void validateMinioBucket(MinioBucket minioBucket) {
        BucketExistsArgs bucketExistArgs = BucketExistsArgs.builder().bucket(minioBucket.getBucketName()).build();
        boolean b = false;
        try {
            b = minioBucket.getClient().bucketExists(bucketExistArgs);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(FileUtils.getRelativePath(RuoYiConfig.getProfile()) + "/")
                    .stream(EmptyInputStream.nullInputStream(), 0, -1).bucket(minioBucket.getBucketName()).build();
            minioBucket.getClient().putObject(putObjectArgs);
        } catch (Exception e) {
            logger.error("数据桶：{}  - 链接失败", minioBucket.getName());
            throw new RuntimeException(e.getMessage());
        }
        if (!b) {
            throw new RuntimeException("Bucket " + minioBucket.getBucketName() + " does not exist");
        }
    }

    private MinioBucket createMinioClient(String name, MinioBucketProperties props) {
        MinioClient client;
        if (StringUtils.isEmpty(props.getAccessKey())) {
            client = MinioClient.builder()
                    .endpoint(props.getUrl())
                    .build();
        } else {
            client = MinioClient.builder()
                    .endpoint(props.getUrl())
                    .credentials(props.getAccessKey(), props.getSecretKey())
                    .build();
        }
        MinioBucket minioBucket = new MinioBucket(client, props.getBucketName(), props.getPermission(), props.getUrl());
        validateMinioBucket(minioBucket);
        logger.info("数据桶：{}  - 链接成功", name);
        return minioBucket;
    }

    @Override
    public MinioBucket getBucket(String clent) {
        return targetMinioBucket.get(clent);
    }

    public MinioBucket getPrimaryBucket() {
        return this.primaryBucket;
    }

    public Map<String, MinioBucketProperties> getClient() {
        return client;
    }

    public void setClient(Map<String, MinioBucketProperties> client) {
        this.client = client;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }
}
