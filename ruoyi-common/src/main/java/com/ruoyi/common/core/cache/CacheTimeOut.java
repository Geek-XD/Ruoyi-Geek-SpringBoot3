package com.ruoyi.common.core.cache;

import java.util.concurrent.TimeUnit;

public interface CacheTimeOut extends CacheNoTimeOut {

    public <T> void setCacheObject(final String cacheName, final String key, final T value, final long timeout,
            final TimeUnit timeUnit);

}
