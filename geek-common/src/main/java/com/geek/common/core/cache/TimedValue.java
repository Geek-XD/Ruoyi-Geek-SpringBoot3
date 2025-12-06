package com.geek.common.core.cache;

import java.sql.Date;

/**
 * 包装带有单次写入 TTL 的值。TTL 毫秒为相对时长，由 ExpiryPolicy 在创建/更新时读取。
 */
public final class TimedValue<T> {
    private final T data;
    private final long ttlMillis;
    private final Date createTime;

    public TimedValue(T data, long ttlMillis) {
        this.data = data;
        this.ttlMillis = ttlMillis;
        this.createTime = new Date(System.currentTimeMillis());
    }

    public T getData() {
        return data;
    }

    public long getTtlMillis() {
        return ttlMillis;
    }

    public boolean isExpired() {
        if (ttlMillis <= 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        return now - createTime.getTime() >= ttlMillis;
    }
}
