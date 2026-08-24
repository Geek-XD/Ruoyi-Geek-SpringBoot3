package com.geek.framework.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.geek.common.core.cache.CacheProviderReport;
import com.geek.common.core.cache.GeekCacheReportProvider;
import com.geek.common.core.cache.GeekCacheReportProviderRegistry;

import lombok.RequiredArgsConstructor;

/**
 * Aggregates the JetCache application report and provider-specific reports.
 */
@Service
@RequiredArgsConstructor
public class GeekCacheReportService {

    private final GeekJetCacheReport jetCacheReport;
    private final GeekCacheReportProviderRegistry reportProviders;

    public Map<String, Object> collect() {
        Map<String, Object> report = new LinkedHashMap<>(3);
        Map<String, Object> jetCache = jetCacheReport.collect();
        report.put("summary", jetCache.get("summary"));
        report.put("caches", jetCache.get("caches"));
        report.put("providers", collectProviderReports());
        return report;
    }

    private List<CacheProviderReport> collectProviderReports() {
        return reportProviders.getProviders().stream()
                .map(this::collectProviderReport)
                .toList();
    }

    private CacheProviderReport collectProviderReport(GeekCacheReportProvider provider) {
        try {
            return provider.collect();
        } catch (RuntimeException ex) {
            return CacheProviderReport.builder()
                    .id(provider.id())
                    .name(provider.id())
                    .category("other")
                    .enabled(true)
                    .available(false)
                    .collectedAt(System.currentTimeMillis())
                    .data(tools.jackson.databind.node.JsonNodeFactory.instance.objectNode())
                    .error(ex.getMessage())
                    .build();
        }
    }
}
