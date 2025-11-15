package com.ruoyi.ehcache.config;

import java.util.concurrent.TimeUnit;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "spring.cache", name = { "type" }, havingValue = "jcache", matchIfMissing = false)
public class Ehcache3Config {

    @Bean
    public JCacheCacheManager ehcacheManager() {
        EhcacheCachingProvider cachingProvider = (EhcacheCachingProvider) Caching.getCachingProvider();

        CacheManager cacheManager = cachingProvider.getCacheManager();
        MutableConfiguration<String, Object> mutableConfiguration = new MutableConfiguration<>();
        mutableConfiguration.setTypes(String.class, Object.class);
        mutableConfiguration.setStoreByValue(false); // 默认值为 true，可根据需求调整
        mutableConfiguration.setManagementEnabled(true); // 启用管理功能（可选）
        mutableConfiguration.setStatisticsEnabled(true); // 启用统计功能（可选）
        // 设置缓存过期策略（以 timeToIdle 为例，根据实际需求调整）
        mutableConfiguration.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 1)));

        cacheManager.createCache("temp_cache", mutableConfiguration);
        cacheManager.createCache("eternal_cache", mutableConfiguration);
        cacheManager.createCache("sys_dict", mutableConfiguration);
        cacheManager.createCache("sys_config", mutableConfiguration);
        cacheManager.createCache("repeat_submit", mutableConfiguration);
        cacheManager.createCache("captcha_codes", mutableConfiguration);
        cacheManager.createCache("login_tokens", mutableConfiguration);
        cacheManager.createCache("ip_err_cnt_key", mutableConfiguration);
        cacheManager.createCache("rate_limit", mutableConfiguration);
        cacheManager.createCache("pwd_err_cnt", mutableConfiguration);

        JCacheCacheManager jCacheCacheManager = new JCacheCacheManager(cacheManager);

        return jCacheCacheManager;
    }
}
