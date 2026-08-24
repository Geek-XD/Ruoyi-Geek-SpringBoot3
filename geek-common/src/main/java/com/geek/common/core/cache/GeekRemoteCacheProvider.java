package com.geek.common.core.cache;

/**
 * Describes an optional remote cache provider without exposing provider-specific APIs.
 */
public interface GeekRemoteCacheProvider {

    String id();

    boolean isReady();
}
