package com.geek.framework.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DynamicStorageBootProperties.class)
public class StorageBucketConfiguration {
    
}
