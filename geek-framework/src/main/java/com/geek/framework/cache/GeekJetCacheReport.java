package com.geek.framework.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.support.CacheStat;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.geek.common.core.cache.GeekRemoteCacheProviderRegistry;

import lombok.RequiredArgsConstructor;

/**
 * Collects the complete JetCache application-level report.
 */
@Component
@RequiredArgsConstructor
public class GeekJetCacheReport {

    private final GeekJetCacheManager cacheManager;
    private final GeekCacheProperties properties;
    private final GeekRemoteCacheProviderRegistry remoteCacheProviders;

    @Value("${jetcache.local.default.type:unknown}")
    private String localProvider;

    @Value("${jetcache.statIntervalMinutes:0}")
    private long statIntervalMinutes;

    public Map<String, Object> collect() {
        List<Map<String, Object>> caches = buildCacheStats();
        Map<String, Object> report = new LinkedHashMap<>(2);
        report.put("summary", buildSummary(caches));
        report.put("caches", caches);
        return report;
    }

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
        String providerId = properties.getRemote().getProvider();
        if (providerId == null || providerId.isBlank()) {
            return null;
        }
        return remoteCacheProviders.findById(providerId).map(provider -> provider.id()).orElse(null);
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
        DefaultCacheMonitor monitor = cacheManager.getCacheMonitors().get(cacheName);
        return monitor == null ? null : monitor.getCacheStat().clone();
    }

    private List<Map<String, Object>> buildCacheStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        List<String> cacheNames = new ArrayList<>(cacheManager.getCacheNames());
        cacheNames.sort(String::compareTo);
        for (String cacheName : cacheNames) {
            CacheStat cacheStat = getCacheStat(cacheName);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("cacheName", cacheName);
            item.put("keyCount", getRegisteredKeyCount(cacheName));
            item.put("cacheType", getCacheType().name());
            item.put("defaultExpire", formatDuration(getDefaultExpire()));
            item.put("localExpire", formatDuration(getLocalExpire()));
            item.put("qps", round(cacheStat == null ? 0D : cacheStat.qps()));
            item.put("hitRate", round((cacheStat == null ? 0D : cacheStat.hitRate()) * 100));
            item.put("getCount", cacheStat == null ? 0L : cacheStat.getGetCount());
            item.put("hitCount", cacheStat == null ? 0L : cacheStat.getGetHitCount());
            item.put("missCount", cacheStat == null ? 0L : cacheStat.getGetMissCount());
            item.put("expireCount", cacheStat == null ? 0L : cacheStat.getGetExpireCount());
            item.put("putCount", cacheStat == null ? 0L : cacheStat.getPutCount());
            item.put("removeCount", cacheStat == null ? 0L : cacheStat.getRemoveCount());
            item.put("loadCount", cacheStat == null ? 0L : cacheStat.getLoadCount());
            stats.add(item);
        }
        return stats;
    }

    private Map<String, Object> buildSummary(List<Map<String, Object>> cacheStats) {
        long totalKeys = sumLong(cacheStats, "keyCount");
        long totalGets = sumLong(cacheStats, "getCount");
        long totalHits = sumLong(cacheStats, "hitCount");
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("area", getArea());
        summary.put("cacheType", getCacheType().name());
        summary.put("localProvider", getLocalProvider());
        String remoteProvider = getRemoteProvider();
        if (remoteProvider != null) summary.put("remoteProvider", remoteProvider);
        summary.put("multiLevelEnabled", isMultiLevelEnabled());
        summary.put("syncLocal", isSyncLocal());
        summary.put("penetrationProtect", isPenetrationProtect());
        summary.put("defaultExpire", formatDuration(getDefaultExpire()));
        summary.put("localExpire", formatDuration(getLocalExpire()));
        summary.put("localLimit", getLocalLimit());
        summary.put("statIntervalMinutes", getStatIntervalMinutes());
        summary.put("cacheCount", cacheStats.size());
        summary.put("activeCacheCount", cacheManager.getCacheNames().size());
        summary.put("applicationKeyCount", totalKeys);
        summary.put("keyCount", totalKeys);
        summary.put("getCount", totalGets);
        summary.put("hitCount", totalHits);
        summary.put("missCount", sumLong(cacheStats, "missCount"));
        summary.put("putCount", sumLong(cacheStats, "putCount"));
        summary.put("removeCount", sumLong(cacheStats, "removeCount"));
        summary.put("loadCount", sumLong(cacheStats, "loadCount"));
        summary.put("hitRate", totalGets == 0 ? 0D : round((double) totalHits * 100 / totalGets));
        summary.put("qps", round(cacheStats.stream().mapToDouble(item -> ((Number) item.get("qps")).doubleValue()).sum()));
        return summary;
    }

    private long sumLong(List<Map<String, Object>> stats, String key) {
        return stats.stream().map(item -> item.get(key)).filter(Number.class::isInstance)
                .map(Number.class::cast).mapToLong(Number::longValue).sum();
    }

    private double round(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) return "0s";
        if (duration.toDays() > 0) return duration.toDays() + "d";
        if (duration.toHours() > 0) return duration.toHours() + "h";
        if (duration.toMinutes() > 0) return duration.toMinutes() + "m";
        return duration.toSeconds() + "s";
    }
}
