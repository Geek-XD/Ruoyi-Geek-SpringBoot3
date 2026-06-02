package com.geek.framework.cache;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "geek.cache")
public class GeekCacheProperties {

    private String area = "default";
    private Duration defaultExpire = Duration.ofDays(3650);
    private Duration localExpire = Duration.ofDays(3650);
    private int localLimit = 1000;
    private boolean syncLocal = true;
    private boolean multiLevelEnabled = true;
    private boolean useAreaInPrefix = false;
    private boolean penetrationProtect = false;
}
