package com.geek.framework.cache;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Duration getDefaultExpire() {
        return defaultExpire;
    }

    public void setDefaultExpire(Duration defaultExpire) {
        this.defaultExpire = defaultExpire;
    }

    public Duration getLocalExpire() {
        return localExpire;
    }

    public void setLocalExpire(Duration localExpire) {
        this.localExpire = localExpire;
    }

    public int getLocalLimit() {
        return localLimit;
    }

    public void setLocalLimit(int localLimit) {
        this.localLimit = localLimit;
    }

    public boolean isSyncLocal() {
        return syncLocal;
    }

    public void setSyncLocal(boolean syncLocal) {
        this.syncLocal = syncLocal;
    }

    public boolean isMultiLevelEnabled() {
        return multiLevelEnabled;
    }

    public void setMultiLevelEnabled(boolean multiLevelEnabled) {
        this.multiLevelEnabled = multiLevelEnabled;
    }

    public boolean isUseAreaInPrefix() {
        return useAreaInPrefix;
    }

    public void setUseAreaInPrefix(boolean useAreaInPrefix) {
        this.useAreaInPrefix = useAreaInPrefix;
    }

    public boolean isPenetrationProtect() {
        return penetrationProtect;
    }

    public void setPenetrationProtect(boolean penetrationProtect) {
        this.penetrationProtect = penetrationProtect;
    }
}
