package com.geek.framework.storage;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.geek.common.config.GeekConfig;
import com.geek.common.core.file.GeekStorageBucket;
import com.geek.common.core.file.storage.StorageBucket;
import com.geek.common.core.file.storage.StorageFactory;

@Configuration
@ConfigurationProperties(prefix = "storage")
public class GeekStorageBootstrap implements InitializingBean {

    @Autowired
    Map<String, StorageFactory<?>> storageFactoryMap;

    private GeekStorageBucket targetBucket;

    @Override
    public void afterPropertiesSet() throws Exception {
        buckets.forEach((name, properties) -> {
            String type = properties.getProperty("type");
            StorageFactory<?> storageFactory = storageFactoryMap.get(type);
            if (storageFactory == null) {
                throw new IllegalStateException("不存在该存储类型的工厂类：" + type);
            }
            StorageBucket bucket = storageFactory.buildBucket(name, properties);
            if (targetBucket == null) {
                targetBucket = new GeekStorageBucket(name, bucket, type);
                GeekConfig.setGeekStorageBucket(targetBucket);
            } else {
                targetBucket.addStorageBucket(name, bucket, type);
            }
        });
    }

    private LinkedMap<String, Properties> buckets = new LinkedMap<>();

    public LinkedMap<String, Properties> getBuckets() {
        return buckets;
    }

    public void setBuckets(LinkedMap<String, Properties> buckets) {
        this.buckets = buckets;
    }
}
