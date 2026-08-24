package com.geek.common.core.cache;

/**
 * Supplies provider-specific cache monitoring data to the unified cache report.
 */
public interface GeekCacheReportProvider {

    String id();

    CacheProviderReport collect();
}
