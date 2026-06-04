package com.geek.framework.cache;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.support.CacheStat;
import com.alicp.jetcache.support.DefaultCacheMonitor;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeekJetCacheReport {

    private final GeekJetCacheManager cacheManager;
    private final GeekCacheProperties properties;

    @Value("${jetcache.local.default.type:unknown}")
    private String localProvider;

    @Value("${jetcache.remote.default.type:#{null}}")
    private @Nullable String remoteProvider;

    @Value("${jetcache.statIntervalMinutes:0}")
    private long statIntervalMinutes;

    public CacheType getCacheType() {
        return cacheManager.resolveCacheType();
    }

    public String getArea() {
        return properties.getArea();
    }

    public Duration getDefaultExpire() {
        return properties.getDefaultExpire();
    }

    public Duration getLocalExpire() {
        return properties.getLocalExpire();
    }

    public int getLocalLimit() {
        return properties.getLocalLimit();
    }

    public boolean isSyncLocal() {
        return properties.isSyncLocal();
    }

    public boolean isMultiLevelEnabled() {
        return properties.isMultiLevelEnabled();
    }

    public boolean isPenetrationProtect() {
        return properties.isPenetrationProtect();
    }

    public String getLocalProvider() {
        return localProvider;
    }

    public @Nullable String getRemoteProvider() {
        return remoteProvider;
    }

    public long getStatIntervalMinutes() {
        return statIntervalMinutes;
    }

    public int getRegisteredKeyCount(String cacheName) {
        ConcurrentMap<String, Set<String>> keyRegistry = cacheManager.getKeyRegistry();
        ConcurrentMap<String, Cache<String, Object>> caches = cacheManager.getCaches();

        Set<String> registeredKeys = keyRegistry.get(cacheName);
        if (registeredKeys == null || registeredKeys.isEmpty()) {
            return 0;
        }
        Cache<String, Object> cache = caches.get(cacheName);
        if (cache == null) {
            return registeredKeys.size();
        }
        int activeKeyCount = 0;
        for (String key : Set.copyOf(registeredKeys)) {
            if (cache.get(key) != null) {
                activeKeyCount++;
            } else {
                registeredKeys.remove(key);
            }
        }
        if (registeredKeys.isEmpty()) {
            keyRegistry.remove(cacheName, registeredKeys);
        }
        return activeKeyCount;
    }

    public @Nullable CacheStat getCacheStat(String cacheName) {
        ConcurrentMap<String, DefaultCacheMonitor> cacheMonitors = cacheManager.getCacheMonitors();
        DefaultCacheMonitor monitor = cacheMonitors.get(cacheName);
        if (monitor == null) {
            return null;
        }
        return monitor.getCacheStat().clone();
    }
}
