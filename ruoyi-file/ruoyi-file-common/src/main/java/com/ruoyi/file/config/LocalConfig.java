package com.ruoyi.file.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ruoyi.file.domain.LocalBucket;
import com.ruoyi.file.storage.StorageConfig;

@Configuration("local")
@ConditionalOnProperty(prefix = "local", name = { "enable" }, havingValue = "true", matchIfMissing = false)
@ConfigurationProperties("local")
public class LocalConfig implements InitializingBean, StorageConfig {
    private static final Logger logger = LoggerFactory.getLogger(LocalConfig.class);
    private Map<String, LocalClientProperties> client;
    private String primary;
    private Map<String, LocalBucket> targetLocalBucket = new HashMap<>();
    private LocalBucket primaryBucket;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (client == null || client.isEmpty()) {
            throw new RuntimeException("Local client properties cannot be null or empty");
        }
        client.forEach((name, props) -> {
            targetLocalBucket.put(name, new LocalBucket(name, props.getPath(), props.getPermission(), props.getApi()));
            logger.info("本地存储目录：{} - 配置成功，路径:{}", name, props.getPath());
        });
        if (targetLocalBucket.get(primary) == null) {
            throw new RuntimeException("Primary local client " + primary + " does not exist");
        }
        primaryBucket = targetLocalBucket.get(primary);
    }

    public LocalBucket getPrimaryBucket() {
        return this.primaryBucket;
    }

    @Override
    public LocalBucket getBucket(String clientName) {
        return targetLocalBucket.get(clientName);
    }

    public Map<String, LocalClientProperties> getClient() {
        return client;
    }

    public void setClient(Map<String, LocalClientProperties> client) {
        this.client = client;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }
}
