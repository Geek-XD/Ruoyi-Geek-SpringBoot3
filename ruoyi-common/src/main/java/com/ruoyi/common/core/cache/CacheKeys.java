package com.ruoyi.common.core.cache;

import java.util.Set;

import org.springframework.cache.Cache;

public interface CacheKeys {

    public Set<String> getCachekeys(final Cache cache);
}
