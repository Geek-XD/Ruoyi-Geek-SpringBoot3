package com.geek.common.utils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;
import org.springframework.util.ObjectUtils;

import com.geek.common.core.cache.GeekCacheManager;
import com.geek.common.utils.spring.SpringUtils;

public class CacheUtils {

    /**
     * 获取CacheManager
     *
     * @return
     */
    public static GeekCacheManager getCacheManager() {
        return SpringUtils.getBean(GeekCacheManager.class);
    }

    /**
     * 获取缓存的所有key值(由于springcache不支持获取所有key,只能根据cache类型来单独获取)
     *
     * @param cacheName
     * @return
     */
    public static Set<String> getkeys(String cacheName) {
        return getCacheManager().getCacheKeys(cacheName);
    }

    /**
     * 根据cacheName,key缓存数据
     *
     * @param cacheName
     * @param key
     * @param value
     * @param <T>
     */
    public static void put(String cacheName, String key, Object value) {
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
    public static void putIfAbsent(String cacheName, String key, Object value) {
        getCacheManager().putIfAbsent(cacheName, key, value);
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
    public static void put(String cacheName, String key, Object value, long timeout, TimeUnit unit) {
        if (timeout != 0 && unit != null) {
            getCacheManager().put(cacheName, key, value, timeout, unit);
        } else {
            getCacheManager().put(cacheName, key, value);
        }
    }

    /**
     * 获取数据
     *
     * @param cacheName
     * @param key
     * @return
     */
    public static Object get(String cacheName, String key) {
        return getCacheManager().get(cacheName, key);
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
        return getCacheManager().get(cacheName, key, type);
    }

    /**
     * 移除缓存数据
     *
     * @param cacheName
     * @param key
     */
    public static void remove(String cacheName, String key) {
        getCacheManager().remove(cacheName, key);
    }

    /**
     * 如果存在则移除缓存数据
     *
     * @param cacheName
     * @param key
     * @return
     */
    public static boolean removeIfPresent(String cacheName, String key) {
        return getCacheManager().remove(cacheName, key);
    }

    /**
     * 清除缓存名称为cacheName的所有缓存数据
     *
     * @param cacheName
     */
    public static void clear(String cacheName) {
        getCacheManager().clear(cacheName);
    }

    public static void clearAll() {
        getCacheManager().clearAll();
    }

    private CacheUtils() {
    }
}
