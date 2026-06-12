package com.geek.framework.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheMonitor;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.template.QuickConfig;
import com.geek.common.core.cache.GeekCacheManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GeekJetCacheManager implements GeekCacheManager {

    private final CacheManager cacheManager;
    private final GeekCacheProperties properties;
    private final Environment environment;

    @Getter
    private final ConcurrentMap<String, Cache<String, Object>> caches = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentMap<String, Set<String>> keyRegistry = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentMap<String, DefaultCacheMonitor> cacheMonitors = new ConcurrentHashMap<>();

    @Override
    public Collection<String> getCacheNames() {
        Set<String> cacheNames = new LinkedHashSet<>(caches.keySet());
        cacheNames.addAll(keyRegistry.keySet());
        return Collections.unmodifiableSet(cacheNames);
    }

    @Override
    public Set<String> getCacheKeys(String cacheName) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        Set<String> registeredKeys = keyRegistry.computeIfAbsent(cacheName, ignored -> ConcurrentHashMap.newKeySet());
        Set<String> activeKeys = new LinkedHashSet<>();
        for (String key : Set.copyOf(registeredKeys)) {
            if (cache.get(key) != null) {
                activeKeys.add(key);
            } else {
                registeredKeys.remove(key);
            }
        }
        return activeKeys;
    }

    @Override
    public <T> void put(String cacheName, String key, T value) {
        executeAfterCommitOrNow(() -> doPut(cacheName, key, value));
    }

    @Override
    public <T> void put(String cacheName, String key, T value, long timeout, TimeUnit timeUnit) {
        executeAfterCommitOrNow(() -> {
            Cache<String, Object> cache = getOrCreateCache(cacheName);
            cache.put(key, value, timeout, timeUnit);
            registerKey(cacheName, key);
        });
    }

    @Override
    public boolean putIfAbsent(String cacheName, String key, Object value) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        boolean inserted = cache.putIfAbsent(key, value);
        if (inserted) {
            registerKey(cacheName, key);
        }
        return inserted;
    }

    @Override
    public @Nullable Object get(String cacheName, String key) {
        Object value = getOrCreateCache(cacheName).get(key);
        if (value == null) {
            unregisterKey(cacheName, key);
        }
        return value;
    }

    @Override
    public <T> T get(String cacheName, String key, @Nullable Class<T> type) {
        Object value = get(cacheName, key);
        if (value == null || type == null) {
            return (T) value;
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("缓存类型不匹配: " + cacheName + ":" + key);
        }
        return type.cast(value);
    }

    @Override
    public boolean remove(String cacheName, String key) {
        boolean existed = get(cacheName, key) != null;
        executeAfterCommitOrNow(() -> {
            getOrCreateCache(cacheName).remove(key);
            unregisterKey(cacheName, key);
        });
        return existed;
    }

    @Override
    public void clear(String cacheName) {
        Set<String> keys = getCacheKeys(cacheName);
        executeAfterCommitOrNow(() -> {
            if (!keys.isEmpty()) {
                getOrCreateCache(cacheName).removeAll(keys);
            }
            keyRegistry.remove(cacheName);
        });
    }

    @Override
    public void clearAll() {
        for (String cacheName : new LinkedHashSet<>(caches.keySet())) {
            clear(cacheName);
        }
    }

    private <T> void doPut(String cacheName, String key, T value) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        cache.put(key, value);
        registerKey(cacheName, key);
    }

    private Cache<String, Object> getOrCreateCache(String cacheName) {
        return caches.computeIfAbsent(cacheName, this::createCache);
    }

    private Cache<String, Object> createCache(String cacheName) {
        CacheType cacheType = resolveCacheType();
        QuickConfig.Builder builder = QuickConfig.newBuilder(properties.getArea(), buildRemoteCacheName(cacheName))
                .expire(properties.getDefaultExpire())
                .cacheType(cacheType)
                .localLimit(properties.getLocalLimit())
                .useAreaInPrefix(properties.isUseAreaInPrefix())
                .penetrationProtect(properties.isPenetrationProtect());
        if (cacheType != CacheType.REMOTE) {
            builder.localExpire(properties.getLocalExpire());
        }
        if (cacheType == CacheType.BOTH) {
            builder.syncLocal(properties.isSyncLocal());
        }
        Cache<String, Object> cache = (Cache<String, Object>) (Cache<?, ?>) cacheManager
                .getOrCreateCache(builder.build());
        DefaultCacheMonitor cacheMonitor = new DefaultCacheMonitor(cacheName);
        List<CacheMonitor> monitors = cache.config().getMonitors();
        List<CacheMonitor> configuredMonitors = monitors == null ? new ArrayList<>() : new ArrayList<>(monitors);
        configuredMonitors.add(cacheMonitor);
        cache.config().setMonitors(configuredMonitors);
        cacheMonitors.put(cacheName, cacheMonitor);
        return cache;
    }

    String buildRemoteCacheName(String cacheName) {
        if (cacheName == null || cacheName.isEmpty()) {
            return cacheName;
        }
        String normalized = cacheName;
        if (normalized.startsWith(":")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith(":")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return ":" + normalized + ":";
    }

    public CacheType resolveCacheType() {
        if (properties.isMultiLevelEnabled() && environment.containsProperty("jetcache.remote.default.type")) {
            return CacheType.BOTH;
        }
        return CacheType.LOCAL;
    }

    CacheType getCacheType() {
        return resolveCacheType();
    }

    private void registerKey(String cacheName, String key) {
        keyRegistry.computeIfAbsent(cacheName, ignored -> ConcurrentHashMap.newKeySet()).add(key);
    }

    private void unregisterKey(String cacheName, String key) {
        Set<String> keys = keyRegistry.get(cacheName);
        if (keys == null) {
            return;
        }
        keys.remove(key);
        if (keys.isEmpty()) {
            keyRegistry.remove(cacheName, keys);
        }
    }

    private void executeAfterCommitOrNow(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
