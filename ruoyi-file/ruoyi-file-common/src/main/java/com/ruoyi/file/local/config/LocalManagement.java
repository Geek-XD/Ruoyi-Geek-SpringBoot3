package com.ruoyi.file.local.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ruoyi.file.local.domain.LocalBucket;
import com.ruoyi.file.storage.StorageManagement;

@Configuration("local")
@ConditionalOnProperty(prefix = "local", name = { "enable" }, havingValue = "true", matchIfMissing = false)
@ConfigurationProperties("local")
public class LocalManagement implements StorageManagement, WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(LocalManagement.class);
    private Map<String, LocalBucketProperties> client;
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

    @Override
    public LocalBucket getBucket(String clientName) {
        return targetLocalBucket.get(clientName);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        client.forEach((name, props) -> {
            if ("public".equals(props.getPermission())) {
                registry.addResourceHandler(props.getApi() + "/**")
                        .addResourceLocations("file:" + props.getPath() + "/");
            }
        });
    }

    public LocalBucket getPrimaryBucket() {
        return this.primaryBucket;
    }

    public Map<String, LocalBucketProperties> getClient() {
        return client;
    }

    public void setClient(Map<String, LocalBucketProperties> client) {
        this.client = client;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }
}
