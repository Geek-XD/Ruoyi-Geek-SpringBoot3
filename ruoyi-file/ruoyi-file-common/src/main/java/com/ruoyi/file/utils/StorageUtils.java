package com.ruoyi.file.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.file.storage.StorageBucket;
import com.ruoyi.file.storage.StorageManagement;
import com.ruoyi.file.storage.StorageService;

import jakarta.annotation.PostConstruct;

@Component
public class StorageUtils {
    private static final Logger logger = LoggerFactory.getLogger(StorageUtils.class);

    private static Map<String, StorageService> storageServiceMap;

    private static Map<String, StorageManagement> storageManagementMap;

    /**
     * 获取指定存储类型和客户端名称的存储桶
     * 
     * @param storageType 存储类型
     * @param clientName  客户端名称
     * @return 存储桶
     */
    public static StorageBucket getStorageBucket(String storageType, String clientName) {
        StorageManagement storageManagement = storageManagementMap.get(storageType);
        if (storageManagement == null) {
            throw new IllegalArgumentException("Storage management for type " + storageType + " not found");
        }
        StorageBucket storageBucket = storageManagement.getBucket(clientName);
        if (storageBucket == null) {
            throw new IllegalArgumentException(
                    "Storage bucket for client " + clientName + " not found in type " + storageType);
        }
        return storageBucket;
    }

    /**
     * 获取所有可用存储渠道及其client列表
     * 
     * @return
     */
    public static Map<String, List<String>> getClientList() {
        Map<String, List<String>> result = new HashMap<>();
        for (String storageType : storageManagementMap.keySet()) {
            StorageManagement config = storageManagementMap.get(storageType);
            result.put(storageType, new ArrayList<>(config.getClient().keySet()));
        }
        return result;
    }

    @Autowired(required = false)
    private void setStorageServiceMap(Map<String, StorageService> storageServiceMap) {
        StorageUtils.storageServiceMap = storageServiceMap;
    }

    @Autowired(required = false)
    private void setStorageManagementMap(Map<String, StorageManagement> storageManagementMap) {
        StorageUtils.storageManagementMap = storageManagementMap;
    }

    @PostConstruct
    private void init() {
        if (StorageUtils.storageServiceMap == null) {
            StorageUtils.storageServiceMap = new HashMap<>();
            logger.warn("请注意，没有加载任何存储服务");
        } else {
            StorageUtils.storageServiceMap.forEach((k, v) -> {
                logger.info("已加载存储服务 {}", k);
            });
        }
    }

}
