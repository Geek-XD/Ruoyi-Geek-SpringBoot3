package com.geek.framework.cache;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geek.common.core.cache.GeekCacheReportProvider;
import com.geek.common.core.cache.GeekCacheReportProviderRegistry;
import com.geek.common.core.cache.GeekRemoteCacheProvider;
import com.geek.common.core.cache.GeekRemoteCacheProviderRegistry;

@Configuration
@EnableConfigurationProperties(GeekCacheProperties.class)
public class GeekCacheConfiguration {

    @Bean
    GeekRemoteCacheProviderRegistry geekRemoteCacheProviderRegistry(List<GeekRemoteCacheProvider> providers) {
        return new GeekRemoteCacheProviderRegistry(providers);
    }

    @Bean
    GeekCacheReportProviderRegistry geekCacheReportProviderRegistry(List<GeekCacheReportProvider> providers) {
        return new GeekCacheReportProviderRegistry(providers);
    }
}
