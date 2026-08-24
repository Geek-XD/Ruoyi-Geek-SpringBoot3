package com.geek.web.controller.monitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

import com.geek.common.core.domain.AjaxResult;
import com.geek.common.core.text.Convert;
import com.geek.common.utils.CacheUtils;
import com.geek.common.utils.StringUtils;
import com.geek.framework.cache.GeekJetCacheManager;
import com.geek.framework.cache.GeekCacheReportService;

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

    @Autowired
    private GeekCacheReportService cacheReportService;

    @Operation(summary = "获取 JetCache 监控概览")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping()
    public AjaxResult getInfo() {
        return AjaxResult.success(cacheReportService.collect());
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
        Map<String, Object> cacheValue = new LinkedHashMap<>(3);
        cacheValue.put("cacheName", cacheName);
        cacheValue.put("cacheKey", cacheKey);
        if (StringUtils.isNotNull(cachedValue)) {
            cacheValue.put("cacheValue", Convert.toStr(cachedValue, ""));
        }
        return AjaxResult.success(cacheValue);
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

    private List<Map<String, Object>> buildCacheCatalog() {
        List<String> cacheNames = new ArrayList<>(cacheManager.getCacheNames());
        cacheNames.sort(String::compareTo);
        List<Map<String, Object>> cacheCatalog = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            Map<String, Object> item = new LinkedHashMap<>(1);
            item.put("cacheName", cacheName);
            cacheCatalog.add(item);
        }
        return cacheCatalog;
    }

}