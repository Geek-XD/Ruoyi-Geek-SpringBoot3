package com.geek.framework.cache;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "geek.cache")
public class GeekCacheProperties {
    /** 缓存区域 */
    private String area = "default";
    /** 默认过期时间 */
    private Duration defaultExpire = Duration.ofDays(3650);
    /** 本地缓存过期时间 */
    private Duration localExpire = Duration.ofDays(3650);
    /** 本地缓存最大数量 */
    private int localLimit = 1000;
    /** 是否同步本地缓存 */
    private boolean syncLocal = true;
    /** 是否启用多级缓存 */
    private boolean multiLevelEnabled = true;
    /** 是否在缓存键前使用区域前缀 */
    private boolean useAreaInPrefix = false;
    /** 是否启用缓存穿透保护 */
    private boolean penetrationProtect = false;
}
