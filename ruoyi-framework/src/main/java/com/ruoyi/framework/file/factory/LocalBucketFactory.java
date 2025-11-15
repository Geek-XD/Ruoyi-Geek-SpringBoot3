package com.ruoyi.framework.file.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ruoyi.common.core.file.storage.StorageFactory;
import com.ruoyi.framework.file.domain.LocalBucket;
import com.ruoyi.framework.file.properties.LocalBucketProperties;

@Configuration("local")
@ConfigurationProperties("local")
@ConditionalOnProperty(prefix = "local", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class LocalBucketFactory extends StorageFactory<LocalBucketProperties, LocalBucket> implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(LocalBucketFactory.class);

    @Override
    public LocalBucket createBucket(String name, LocalBucketProperties props) {
        LocalBucket bucket = LocalBucket.builder()
                .bucketName(name)
                .basePath(props.getPath())
                .permission(props.getPermission())
                .api(props.getApi())
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
        getProperties().forEach((name, props) -> {
            if ("public".equals(props.getPermission())) {
                registry.addResourceHandler(props.getApi() + "/**")
                        .addResourceLocations("file:" + props.getPath() + "/");
            }
        });
    }
}
