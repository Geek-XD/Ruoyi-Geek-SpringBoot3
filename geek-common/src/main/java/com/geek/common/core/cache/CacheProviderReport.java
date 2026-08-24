package com.geek.common.core.cache;

import tools.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.Value;

/**
 * Provider-specific cache report data aggregated by the unified endpoint.
 */
@Value
@Builder
public class CacheProviderReport {

    String id;
    String name;
    String category;
    boolean enabled;
    boolean available;
    long collectedAt;
    JsonNode data;
    String error;
}
