package com.geek.web.controller.monitor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alicp.jetcache.support.CacheStat;
import com.geek.common.constant.CacheConstants;
import com.geek.common.core.domain.AjaxResult;
import com.geek.common.core.text.Convert;
import com.geek.common.utils.CacheUtils;
import com.geek.common.utils.StringUtils;
import com.geek.framework.cache.GeekJetCacheManager;
import com.geek.system.domain.SysCache;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 缓存监控
 * 
 * @author geek
 */
@Tag(name = "缓存监控")
@RestController
@RequestMapping("/monitor/cache")
public class CacheController {

    @Autowired
    private GeekJetCacheManager cacheManager;

    private final static List<SysCache> caches = new ArrayList<>();
    {
        caches.add(new SysCache(CacheConstants.LOGIN_TOKEN_KEY, "用户信息"));
        caches.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "配置信息"));
        caches.add(new SysCache(CacheConstants.SYS_DICT_KEY, "数据字典"));
        caches.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
        caches.add(new SysCache(CacheConstants.PHONE_CODES, "短信验证码"));
        caches.add(new SysCache(CacheConstants.EMAIL_CODES, "邮箱验证码"));
        caches.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
        caches.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
        caches.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));
        caches.add(new SysCache(CacheConstants.IP_ERR_CNT_KEY, "IP错误次数"));
        caches.add(new SysCache(CacheConstants.FILE_MD5_PATH_KEY, "path-md5"));
        caches.add(new SysCache(CacheConstants.FILE_PATH_MD5_KEY, "md5-path"));
    }

    @Operation(summary = "获取 JetCache 监控概览")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping()
    public AjaxResult getInfo() {
        List<Map<String, Object>> cacheStats = buildCacheStats();
        Map<String, Object> result = new LinkedHashMap<>(2);
        result.put("summary", buildSummary(cacheStats));
        result.put("caches", cacheStats);
        return AjaxResult.success(result);
    }

    @Operation(summary = "获取缓存名列表")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getNames")
    public AjaxResult cache() {
        return AjaxResult.success(buildCacheCatalog());
    }

    @Operation(summary = "获取缓存键列表")
    @Parameters({
            @Parameter(name = "cacheName", description = "缓存名称", required = true),
    })
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getKeys/{cacheName}")
    public AjaxResult getCacheKeys(@PathVariable String cacheName) {
        Set<String> keyset = CacheUtils.getkeys(cacheName);
        return AjaxResult.success(keyset);
    }

    @Operation(summary = "获取缓存值列表")
    @Parameters({
            @Parameter(name = "cacheName", description = "缓存名称", required = true),
            @Parameter(name = "cacheKey", description = "缓存键名", required = true)
    })
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public AjaxResult getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
        Object cachedValue = CacheUtils.get(cacheName, cacheKey);
        SysCache sysCache = new SysCache();
        sysCache.setCacheName(cacheName);
        sysCache.setCacheKey(cacheKey);
        if (StringUtils.isNotNull(cachedValue)) {
            sysCache.setCacheValue(Convert.toStr(cachedValue, ""));
        }
        return AjaxResult.success(sysCache);
    }

    @Operation(summary = "清除缓存")
    @Parameters({
            @Parameter(name = "cacheName", description = "缓存名称", required = true)
    })
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public AjaxResult clearCacheName(@PathVariable String cacheName) {
        CacheUtils.clear(cacheName);
        return AjaxResult.success();
    }

    @Operation(summary = "清除缓存值")
    @Parameters({
            @Parameter(name = "cacheKey", description = "缓存键名", required = true)
    })
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheName}/{cacheKey}")
    public AjaxResult clearCacheKey(@PathVariable String cacheName, @PathVariable String cacheKey) {
        CacheUtils.removeIfPresent(cacheName, cacheKey);
        return AjaxResult.success();
    }

    @Operation(summary = "清除缓存值(兼容旧接口)")
    @Parameters({
            @Parameter(name = "cacheKey", description = "缓存键名", required = true)
    })
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public AjaxResult clearCacheKey(@PathVariable String cacheKey) {
        return AjaxResult.error("请携带缓存名称调用新接口清理缓存键");
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheAll")
    public AjaxResult clearCacheAll() {
        CacheUtils.clearAll();
        return AjaxResult.success();
    }

    private List<SysCache> buildCacheCatalog() {
        Map<String, SysCache> cacheCatalog = new LinkedHashMap<>();
        for (SysCache cache : caches) {
            cacheCatalog.put(cache.getCacheName(), new SysCache(cache.getCacheName(), cache.getRemark()));
        }
        for (String cacheName : cacheManager.getCacheNames()) {
            cacheCatalog.computeIfAbsent(cacheName, ignored -> new SysCache(cacheName, "业务缓存"));
        }
        return new ArrayList<>(cacheCatalog.values());
    }

    private List<Map<String, Object>> buildCacheStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        for (SysCache cache : buildCacheCatalog()) {
            CacheStat cacheStat = cacheManager.getCacheStat(cache.getCacheName());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("cacheName", cache.getCacheName());
            item.put("remark", cache.getRemark());
            item.put("keyCount", cacheManager.getRegisteredKeyCount(cache.getCacheName()));
            item.put("cacheType", cacheManager.getCacheType().name());
            item.put("defaultExpire", formatDuration(cacheManager.getDefaultExpire()));
            item.put("localExpire", formatDuration(cacheManager.getLocalExpire()));
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
        long totalMisses = sumLong(cacheStats, "missCount");
        long totalPuts = sumLong(cacheStats, "putCount");
        long totalRemoves = sumLong(cacheStats, "removeCount");
        long totalLoads = sumLong(cacheStats, "loadCount");
        double totalQps = cacheStats.stream()
                .mapToDouble(item -> ((Number) item.get("qps")).doubleValue())
                .sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("area", cacheManager.getArea());
        summary.put("cacheType", cacheManager.getCacheType().name());
        summary.put("localProvider", cacheManager.getLocalProvider());
        summary.put("remoteProvider", cacheManager.getRemoteProvider());
        summary.put("multiLevelEnabled", cacheManager.isMultiLevelEnabled());
        summary.put("syncLocal", cacheManager.isSyncLocal());
        summary.put("penetrationProtect", cacheManager.isPenetrationProtect());
        summary.put("defaultExpire", formatDuration(cacheManager.getDefaultExpire()));
        summary.put("localExpire", formatDuration(cacheManager.getLocalExpire()));
        summary.put("localLimit", cacheManager.getLocalLimit());
        summary.put("statIntervalMinutes", cacheManager.getStatIntervalMinutes());
        summary.put("cacheCount", cacheStats.size());
        summary.put("activeCacheCount", cacheManager.getCacheNames().size());
        summary.put("keyCount", totalKeys);
        summary.put("getCount", totalGets);
        summary.put("hitCount", totalHits);
        summary.put("missCount", totalMisses);
        summary.put("putCount", totalPuts);
        summary.put("removeCount", totalRemoves);
        summary.put("loadCount", totalLoads);
        summary.put("hitRate", totalGets == 0 ? 0D : round((double) totalHits * 100 / totalGets));
        summary.put("qps", round(totalQps));
        return summary;
    }

    private long sumLong(List<Map<String, Object>> cacheStats, String key) {
        return cacheStats.stream()
                .map(item -> item.get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToLong(Number::longValue)
                .sum();
    }

    private double round(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return "0s";
        }
        if (duration.toDaysPart() == 0 && duration.toDays() > 0) {
            return duration.toDays() + "d";
        }
        if (duration.toDays() > 0) {
            return duration.toDays() + "d";
        }
        if (duration.toHours() > 0) {
            return duration.toHours() + "h";
        }
        if (duration.toMinutes() > 0) {
            return duration.toMinutes() + "m";
        }
        return duration.toSeconds() + "s";
    }
}
