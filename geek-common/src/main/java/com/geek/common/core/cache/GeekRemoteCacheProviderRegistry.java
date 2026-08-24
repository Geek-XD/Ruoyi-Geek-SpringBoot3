package com.geek.common.core.cache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides provider-neutral lookup of optional remote cache providers.
 */
public final class GeekRemoteCacheProviderRegistry {

    private final Map<String, GeekRemoteCacheProvider> providersById;

    public GeekRemoteCacheProviderRegistry(Collection<? extends GeekRemoteCacheProvider> providers) {
        Map<String, GeekRemoteCacheProvider> indexedProviders = new LinkedHashMap<>();
        for (GeekRemoteCacheProvider provider : providers) {
            GeekRemoteCacheProvider previous = indexedProviders.putIfAbsent(provider.id(), provider);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate remote cache provider id: " + provider.id());
            }
        }
        providersById = Map.copyOf(indexedProviders);
    }

    public Optional<GeekRemoteCacheProvider> findById(String id) {
        return Optional.ofNullable(providersById.get(id));
    }
}
