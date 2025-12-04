package com.ruoyi.framework.storage.factory;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ruoyi.common.core.file.storage.StorageFactory;
import com.ruoyi.framework.storage.domain.LocalBucket;

@Configuration("local")
@ConfigurationProperties("local")
public class LocalBucketFactory extends StorageFactory<LocalBucket> implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(LocalBucketFactory.class);

    @Override
    public LocalBucket createBucket(String name, Properties props) {
        LocalBucket bucket = LocalBucket.builder()
                .bucketName(name)
                .basePath(props.getProperty("path"))
                .permission(props.getProperty("permission"))
                .api(props.getProperty("api"))
                .build();
        logger.info("本地 数据桶：{}  - 创建成功", name);
        return bucket;
    }

    @Override
    public void validateBucket(LocalBucket localBucket) {
        // 本地存储无需校验
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        storageBucketMap.forEach((name, bucket) -> {
            if ("public".equals(bucket.getPermission())) {
                registry.addResourceHandler(bucket.getApi() + "/**")
                        .addResourceLocations("file:" + bucket.getBasePath() + "/");
            }
        });
    }
}
