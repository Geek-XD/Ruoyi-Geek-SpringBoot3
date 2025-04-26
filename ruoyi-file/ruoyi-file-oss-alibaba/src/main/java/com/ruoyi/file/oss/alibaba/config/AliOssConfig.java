package com.ruoyi.file.oss.alibaba.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.ruoyi.file.oss.alibaba.domain.AliOssBucket;
import com.ruoyi.file.oss.alibaba.exception.AliOssClientErrorException;
import com.ruoyi.file.oss.alibaba.exception.AliOssClientNotFundException;
import com.ruoyi.file.storage.StorageConfig;

/**
 * 配置类用于管理阿里云OSS客户端实例及其相关属性。
 */
@Configuration("oss")
@ConditionalOnProperty(prefix = "oss", name = "enable", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "oss")
public class AliOssConfig implements InitializingBean, StorageConfig {
    private static final Logger logger = LoggerFactory.getLogger(AliOssConfig.class);

    public static int maxSize; // 最大文件大小或其他配置项

    private String prefix = "/oss"; // 根据需要调整前缀

    private Map<String, AliOssProperties> client = new HashMap<>(); // 存储所有OSS客户端配置信息

    private String primary; // 主要使用的OSS客户端名称

    private Map<String, AliOssBucket> targetAliOssBucket = new HashMap<>(); // 存储已创建的OSS客户端与桶名映射

    private AliOssBucket masterBucket; // 主要使用的OSS客户端对应的桶对象

    /**
     * 初始化后检查客户端配置是否正确，并创建相应的OSS客户端实例。
     */
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
        masterBucket = targetAliOssBucket.get(primary);
    }

    /**
     * 创建并返回一个指定名称的OSS客户端实例。
     *
     * @param name  客户端名称
     * @param props 客户端配置属性
     * @return 包含OSS客户端和桶名的对象
     */
    private AliOssBucket createOssClient(String name, AliOssProperties props) {
        if (props == null || props.getEndpoint() == null || props.getAccessKeyId() == null ||
                props.getAccessKeySecret() == null || props.getBucketName() == null) {
            throw new IllegalArgumentException("AliOssProperties or its required fields cannot be null");
        }

        OSS client = new OSSClientBuilder().build(props.getEndpoint(), props.getAccessKeyId(),
                props.getAccessKeySecret());
        AliOssBucket ossBucket = new AliOssBucket(client, props.getBucketName());
        validateOssBucket(ossBucket);
        logger.info("数据桶：{} - 链接成功", name);
        return ossBucket;
    }

    /**
     * 验证给定的OSS桶是否存在。
     *
     * @param aliOssBucket 包含OSS客户端和桶名的对象
     */
    private static void validateOssBucket(AliOssBucket aliOssBucket) {
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

    /**
     * 获取主桶的名称。
     *
     * @return 主桶的名称，如果未设置则返回null
     */
    public String getMasterBucketName() {
        if (masterBucket != null) {
            return masterBucket.getBucketName();
        }
        return null; // 或者抛出异常
    }

    /**
     * 根据客户端名称获取对应的桶名。
     *
     * @param client 客户端名称
     * @return 对应的桶名，如果未找到则返回null
     */
    public String getBucketName(String client) {
        if (client != null && targetAliOssBucket.containsKey(client)) {
            return targetAliOssBucket.get(client).getBucketName();
        }
        return null; // 或者抛出异常
    }

    /**
     * 根据主配置信息创建并返回OSS客户端实例。
     *
     * @return OSS客户端实例
     * @throws AliOssClientNotFundException 如果找不到对应配置时抛出
     * @throws AliOssClientErrorException   如果在创建过程中发生错误时抛出
     */
    public OSS getPrimaryOssClient() throws AliOssClientNotFundException, AliOssClientErrorException {
        AliOssProperties primaryClientProps = this.getClient().get(this.getPrimary());
        if (primaryClientProps == null) {
            throw new AliOssClientNotFundException("未找到该Oss对象存储服务！");
        }
        try {
            return new OSSClientBuilder().build(
                    primaryClientProps.getEndpoint(),
                    primaryClientProps.getAccessKeyId(),
                    primaryClientProps.getAccessKeySecret());
        } catch (Exception e) {
            // 在尝试构建OSS客户端时发生任何异常，抛出 AliOssClientErrorException
            throw new AliOssClientErrorException("创建OSS客户端失败", e);
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public AliOssBucket getMasterBucket() {
        return this.masterBucket;
    }

    public AliOssBucket getBucket(String client) {
        return targetAliOssBucket.get(client);
    }

    public Map<String, AliOssProperties> getClient() {
        return client;
    }

    public void setClient(Map<String, AliOssProperties> client) {
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
