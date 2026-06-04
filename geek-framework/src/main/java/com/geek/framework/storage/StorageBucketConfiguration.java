package com.geek.framework.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DynamicStorageBucketProperties.class)
public class StorageBucketConfiguration {
    
}
