package com.geek.common.core.cache;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

public interface GeekCacheManager {

    Collection<String> getCacheNames();

    Set<String> getCacheKeys(String cacheName);

    <T> void put(String cacheName, String key, T value);

    <T> void put(String cacheName, String key, T value, long timeout, TimeUnit timeUnit);

    boolean putIfAbsent(String cacheName, String key, Object value);

    @Nullable
    Object get(String cacheName, String key);

    <T> T get(String cacheName, String key, @Nullable Class<T> type);

    boolean remove(String cacheName, String key);

    void clear(String cacheName);

    void clearAll();
}
