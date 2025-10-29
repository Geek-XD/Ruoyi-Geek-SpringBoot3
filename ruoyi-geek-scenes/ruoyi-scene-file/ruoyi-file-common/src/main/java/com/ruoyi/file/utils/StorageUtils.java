package com.ruoyi.file.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.file.storage.StorageBucket;
import com.ruoyi.file.storage.StorageFactory;

import jakarta.annotation.PostConstruct;

@Component
public class StorageUtils {
    private static final Logger logger = LoggerFactory.getLogger(StorageUtils.class);

    private static Map<String, StorageFactory<?, ?>> storageFactoryMap;

    public static String getPrimaryStorageType() {
        return RuoYiConfig.getFileServer();
    }

    public static StorageBucket getPrimaryStorageBucket() {
        return storageFactoryMap.get(RuoYiConfig.getFileServer()).getPrimaryBucket();
    }

    /**
     * 获取指定存储类型和存储桶名称的存储桶
     * 
     * @param storageType 存储类型
     * @param bucketName  存储桶名称
     * @return 存储桶
     */
    public static StorageBucket getStorageBucket(String storageType, String bucketName) {
        StorageFactory<?, ?> storageFactory = storageFactoryMap.get(storageType);
        if (storageFactory == null) {
            throw new IllegalArgumentException("Storage management for type " + storageType + " not found");
        }
        StorageBucket storageBucket = storageFactory.getBucket(bucketName);
        if (storageBucket == null) {
            throw new IllegalArgumentException(String.format("StorageBucket %s not found in type %s",
                    bucketName, storageType));
        }
        return storageBucket;
    }

    /**
     * 获取所有可用存储渠道及其存储桶列表
     * 
     * @return
     */
    public static Map<String, List<String>> getClientList() {
        Map<String, List<String>> result = new HashMap<>();
        for (String storageType : storageFactoryMap.keySet()) {
            StorageFactory<?, ?> config = storageFactoryMap.get(storageType);
            result.put(storageType, new ArrayList<>(config.getBuckets()));
        }
        return result;
    }

    @Autowired(required = false)
    private void setStorageFactoryMap(Map<String, StorageFactory<?, ?>> storageFactoryMap) {
        StorageUtils.storageFactoryMap = storageFactoryMap;
    }

    @PostConstruct
    private void init() {
        if (StorageUtils.storageFactoryMap == null) {
            StorageUtils.storageFactoryMap = new HashMap<>();
            logger.warn("请注意，没有加载任何存储服务");
        } else {
            StorageUtils.storageFactoryMap.forEach((k, v) -> {
                logger.info("已加载存储服务 {}", k);
            });
        }
    }

}
