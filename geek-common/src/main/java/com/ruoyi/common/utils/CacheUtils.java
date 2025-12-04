package com.ruoyi.common.utils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import com.ruoyi.common.core.cache.TtlCacheManager;
import com.ruoyi.common.utils.spring.SpringUtils;

public class CacheUtils {

    /**
     * 获取CacheManager
     *
     * @return
     */
    public static TtlCacheManager getCacheManager() {
        return SpringUtils.getBean(TtlCacheManager.class);
    }

    /**
     * 根据cacheName从CacheManager中获取cache
     *
     * @param cacheName
     * @return
     */
    public static Cache getCache(String cacheName) {
        return getCacheManager().getCache(cacheName);
    }

    /**
     * 获取缓存的所有key值(由于springcache不支持获取所有key,只能根据cache类型来单独获取)
     *
     * @param cacheName
     * @return
     */
    public static Set<String> getkeys(String cacheName) {
        Cache cache = getCache(cacheName);
        return getCacheManager().getCachekeys(cache);
    }

    /**
     * 根据cacheName,key缓存数据
     *
     * @param cacheName
     * @param key
     * @param value
     * @param <T>
     */
    public static <T> void put(String cacheName, String key, T value) {
        put(cacheName, key, value, 0, null);
    }

    /**
     * 如果没有则进行缓存,根据cacheName,key缓存数据
     *
     * @param cacheName
     * @param key
     * @param value
     * @param <T>
     */
    public static <T> void putIfAbsent(String cacheName, String key, T value) {
        if (ObjectUtils.isEmpty(get(cacheName, key))) {
            put(cacheName, key, value, 0, null);
        }
    }

    public static boolean hasKey(String cacheName, String key) {
        return !ObjectUtils.isEmpty(get(cacheName, key));
    }

    /**
     * 根据cacheName,key和缓存过期时间进行缓存数据,使用各种不同缓存可以单独进行操作
     *
     * @param cacheName
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * @param <T>
     */
    public static <T> void put(String cacheName, String key, T value, long timeout, TimeUnit unit) {
        if (timeout != 0 && unit != null) {
            getCacheManager().setCacheObject(cacheName, key, value, timeout, unit);
        } else {
            getCacheManager().setCacheObject(cacheName, key, value);
        }
    }

    /**
     * 获取数据
     *
     * @param cacheName
     * @param key
     * @return
     */
    public static Cache.ValueWrapper get(String cacheName, String key) {
        return getCache(cacheName).get(key);
    }

    /**
     * 根据类型获取数据
     *
     * @param cacheName
     * @param key
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T get(String cacheName, String key, @Nullable Class<T> type) {
        return getCache(cacheName).get(key, type);
    }

    /**
     * 移除缓存数据
     *
     * @param cacheName
     * @param key
     */
    public static void remove(String cacheName, String key) {
        getCache(cacheName).evict(key);
    }

    /**
     * 如果存在则移除缓存数据
     *
     * @param cacheName
     * @param key
     * @return
     */
    public static boolean removeIfPresent(String cacheName, String key) {
        remove(cacheName, key);
        return false;
    }

    /**
     * 清除缓存名称为cacheName的所有缓存数据
     *
     * @param cacheName
     */
    public static void clear(String cacheName) {
        getCache(cacheName).clear();
    }
}
