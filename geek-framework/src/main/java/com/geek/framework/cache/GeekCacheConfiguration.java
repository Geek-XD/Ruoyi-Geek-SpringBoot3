package com.geek.framework.cache;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeekCacheProperties.class)
public class GeekCacheConfiguration {
}
