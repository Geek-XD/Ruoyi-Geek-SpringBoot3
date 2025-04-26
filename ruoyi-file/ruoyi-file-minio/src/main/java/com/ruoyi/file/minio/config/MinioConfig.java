package com.ruoyi.file.minio.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.io.EmptyInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.file.minio.domain.MinioBucket;
import com.ruoyi.file.minio.exception.MinioClientErrorException;
import com.ruoyi.file.minio.exception.MinioClientNotFundException;
import com.ruoyi.file.storage.StorageConfig;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Configuration("minio")
@ConditionalOnProperty(prefix = "minio", name = { "enable" }, havingValue = "true", matchIfMissing = false)
@ConfigurationProperties("minio")
public class MinioConfig implements InitializingBean, StorageConfig {
    private static final Logger logger = LoggerFactory.getLogger(MinioConfig.class);
    public static int maxSize;
    private String prefix = "/minio";
    private Map<String, MinioClientProperties> client;
    private String primary;
    private Map<String, MinioBucket> targetMinioBucket = new HashMap<>();
    private MinioBucket masterBucket;

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
        masterBucket = targetMinioBucket.get(primary);
    }

    private static void validateMinioBucket(MinioBucket minioBucket) {
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

    private MinioBucket createMinioClient(String name, MinioClientProperties props) {
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
        MinioBucket minioBucket = new MinioBucket(client, props.getBucketName());
        validateMinioBucket(minioBucket);
        logger.info("数据桶：{}  - 链接成功", name);
        return minioBucket;
    }

    /**
     * 根据主配置信息创建并返回MinIO客户端实例。
     *
     * @return MinioClient 实例
     * @throws MinioClientNotFundException 如果找不到对应的配置时抛出
     * @throws MinioClientErrorException   如果在创建过程中发生错误时抛出
     */
    public MinioClient getPrimaryMinioClient() throws MinioClientNotFundException, MinioClientErrorException {
        MinioClientProperties primaryClientProps = this.getClient().get(this.getPrimary());
        if (primaryClientProps == null) {
            throw new MinioClientNotFundException("未找到该Minio对象存储服务！");
        }
        try {
            return MinioClient.builder()
                    .endpoint(primaryClientProps.getUrl())
                    .credentials(primaryClientProps.getAccessKey(), primaryClientProps.getSecretKey())
                    .build();
        } catch (Exception e) {
            throw new MinioClientErrorException("创建MinIO客户端失败: " + e.getMessage(), e);
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public MinioBucket getMasterBucket() {
        return this.masterBucket;
    }

    public MinioBucket getBucket(String clent) {
        return targetMinioBucket.get(clent);
    }

    public Map<String, MinioClientProperties> getClient() {
        return client;
    }

    public void setClient(Map<String, MinioClientProperties> client) {
        this.client = client;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
