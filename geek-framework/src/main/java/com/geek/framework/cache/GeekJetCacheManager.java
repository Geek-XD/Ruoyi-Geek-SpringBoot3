package com.geek.framework.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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
import com.alicp.jetcache.template.QuickConfig;
import com.geek.common.core.cache.GeekCacheManager;
import com.alicp.jetcache.anno.CacheType;

@Component
@SuppressWarnings("unchecked")
public class GeekJetCacheManager implements GeekCacheManager {

    private final com.alicp.jetcache.CacheManager cacheManager;
    private final GeekCacheProperties properties;
    private final Environment environment;
    private final ConcurrentMap<String, Cache<String, Object>> caches = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> keyRegistry = new ConcurrentHashMap<>();

    public GeekJetCacheManager(com.alicp.jetcache.CacheManager cacheManager, GeekCacheProperties properties,
            Environment environment) {
        this.cacheManager = cacheManager;
        this.properties = properties;
        this.environment = environment;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caches.keySet());
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
        QuickConfig.Builder builder = QuickConfig.newBuilder(properties.getArea(), cacheName)
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
        return (Cache<String, Object>) (Cache<?, ?>) cacheManager.getOrCreateCache(builder.build());
    }

    private CacheType resolveCacheType() {
        if (properties.isMultiLevelEnabled() && environment.containsProperty("jetcache.remote.default.type")) {
            return CacheType.BOTH;
        }
        return CacheType.LOCAL;
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
