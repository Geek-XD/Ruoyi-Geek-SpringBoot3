package com.geek.framework.storage;

import java.util.Properties;

import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "storage")
public class DynamicStorageBucketProperties {
    private LinkedMap<String, Properties> buckets = new LinkedMap<>();
    private String primary;
}
