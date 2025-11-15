package com.ruoyi.framework.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ruoyi.common.core.cache.CacheKeys;

@Configuration
@ConditionalOnProperty(prefix = "spring.cache", name = { "type" }, havingValue = "simple", matchIfMissing = false)
public class SimpleCacheManager implements CacheKeys {
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.List.of(
                "temp_cache",
                "eternal_cache",
                "sys_dict",
                "sys_config",
                "repeat_submit",
                "captcha_codes",
                "login_tokens",
                "ip_err_cnt_key",
                "rate_limit",
                "pwd_err_cnt"));
        return cacheManager;
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public Set<String> getCachekeys(Cache cache) {
        Set<String> keyset = new HashSet<>();

        Cache target = cache;
        if (target instanceof TransactionAwareCacheDecorator) {
            target = ((TransactionAwareCacheDecorator) target).getTargetCache();
        }

        if (target instanceof ConcurrentMapCache) {
            Object nativeCache = ((ConcurrentMapCache) target).getNativeCache();
            if (nativeCache instanceof ConcurrentMap) {
                ConcurrentMap map = (ConcurrentMap) nativeCache;
                for (Object k : map.keySet()) {
                    if (k != null) {
                        keyset.add(String.valueOf(k));
                    }
                }
            }
        }

        return keyset;
    }
}
