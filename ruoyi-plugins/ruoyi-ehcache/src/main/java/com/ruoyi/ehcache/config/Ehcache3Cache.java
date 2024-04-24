package com.ruoyi.ehcache.config;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ehcache.core.EhcacheBase;
import org.ehcache.impl.internal.store.heap.OnHeapStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.jcache.JCacheCache;
import org.springframework.cache.jcache.JCacheCacheManager;

import com.ruoyi.common.interceptor.cache.CacheKeys;
import com.ruoyi.common.interceptor.cache.CacheNoTimeOut;

public class Ehcache3Cache implements CacheNoTimeOut, CacheKeys {

    @Autowired
    private JCacheCacheManager jCacheCacheManager;

    @Override
    public Set<String> getCachekeys(Cache cache) {
        Set<String> keyset = new HashSet<>();
        try {
            JCacheCache jehcache = (JCacheCache) cache;
            // org.ehcache.jsr107.Eh107Cache 不公开
            Object nativeCache = jehcache.getNativeCache();
            Class<?> nativeCacheClass = nativeCache.getClass();
            Field ehCacheField = nativeCacheClass.getDeclaredField("ehCache");
            ehCacheField.setAccessible(true);
            EhcacheBase ehcache = (EhcacheBase) ehCacheField.get(nativeCache);
            Field storeField = EhcacheBase.class.getDeclaredField("store");
            storeField.setAccessible(true);
            OnHeapStore store = (OnHeapStore) storeField.get(ehcache);
            Field mapField = OnHeapStore.class.getDeclaredField("map");
            mapField.setAccessible(true);
            // org.ehcache.impl.internal.store.heap.Backend 不公开
            Object map = mapField.get(store);
            Class<?> mapClass = map.getClass();
            Field realMapField = mapClass.getDeclaredField("realMap");
            realMapField.setAccessible(true);
            ConcurrentHashMap<String, ?> realMap = (ConcurrentHashMap<String, ?>) realMapField.get(map);
            keyset = realMap.keySet();
        } catch (Exception e) {
        }
        return keyset;
    }

    @Override
    public <T> void setCacheObject(String cacheName, String key, T value) {
        Cache cache = jCacheCacheManager.getCache(cacheName);
        cache.put(cacheName, value);
    }

}