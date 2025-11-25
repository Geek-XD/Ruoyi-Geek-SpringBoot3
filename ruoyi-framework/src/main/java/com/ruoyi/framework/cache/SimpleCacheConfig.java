package com.ruoyi.framework.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.cache", name = { "type" }, havingValue = "simple", matchIfMissing = false)
public class SimpleCacheConfig {

    @Bean
    public SimpleCacheManager cacheManager() {
        return new SimpleCacheManager();
    }

}
