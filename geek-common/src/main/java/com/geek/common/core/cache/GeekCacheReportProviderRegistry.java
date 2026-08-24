package com.geek.common.core.cache;

import java.util.Collection;
import java.util.List;

/**
 * Holds all optional cache report providers discovered from the application context.
 */
public final class GeekCacheReportProviderRegistry {

    private final List<GeekCacheReportProvider> providers;

    public GeekCacheReportProviderRegistry(Collection<? extends GeekCacheReportProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public List<GeekCacheReportProvider> getProviders() {
        return providers;
    }
}
